package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageProfile = findViewById(R.id.imageProfile);

        imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageProfile.setImageURI(imageUri);
            uploadImageToSupabase(imageUri);
        }
    }

    private void uploadImageToSupabase(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageData = byteBuffer.toByteArray();
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            String userId = getSharedPreferences("login_pref", MODE_PRIVATE).getString("user_id", null);

            if (userId == null) {
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String bucket = "profilepicture"; // Pastikan ini sesuai dengan nama bucket Anda di Supabase
            String fileName = UUID.randomUUID() + ".jpg";
            String objectPath = bucket + "/" + userId + "/" + fileName;
            String uploadUrl = "https://oteebvgtsvgrkfooinrv.supabase.co/storage/v1/object/" + bucket + "/" + userId + "/" + fileName + "?upsert=true";



            RequestBody body = RequestBody.create(imageData, MediaType.parse("image/jpeg"));

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("apikey", getApiKey())
                    .addHeader("Authorization", "Bearer " + getApiKey())
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        saveProfileUrlToSupabase(objectPath); // Kirim path untuk disimpan di DB
                    } else {
                        Log.e("UPLOAD", "Failed (" + response.code() + "): " + responseBody);
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload gagal: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void saveProfileUrlToSupabase(String objectPath) {
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userId = pref.getString("user_id", null);

        if (userId == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            json.put("profile_picture", objectPath); // Simpan path file lengkap (user_id/foto.jpg)
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://oteebvgtsvgrkfooinrv.supabase.co/rest/v1/clients?user_id=eq." + userId)
                .addHeader("apikey", getApiKey())
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Gagal menyimpan ke database", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Foto profil berhasil disimpan", Toast.LENGTH_SHORT).show();
                        getSignedUrlFromSupabase(objectPath); // Mendapatkan signed URL
                    });
                } else {
                    Log.e("SAVE_URL", "Failed: " + resp);
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Gagal update DB", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getSignedUrlFromSupabase(String objectPath) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("expiresIn", 3600); // 1 jam
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://oteebvgtsvgrkfooinrv.supabase.co/storage/v1/object/sign/" + objectPath)
                .addHeader("apikey", getApiKey())
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Gagal mendapatkan signed URL", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String signedUrl = "https://oteebvgtsvgrkfooinrv.supabase.co/storage/v1/" + jsonObject.getString("signedURL");

                        runOnUiThread(() -> {
                            Glide.with(ProfileActivity.this)
                                    .load(signedUrl)
                                    .into(imageProfile); // Menampilkan gambar
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String error = response.body().string();
                    Log.e("SIGNED_URL", "Error: " + response.code() + " - " + error);
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Gagal load gambar dari signed URL", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getApiKey() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90ZWVidmd0c3Zncmtmb29pbnJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNDU1NzQsImV4cCI6MjA1ODYyMTU3NH0.IE1UReAZZk-9fbqi8SV3EF86Py703eoJVvpEBbzCBAo";  // Ganti dengan API Key yang sesuai
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void enableFullscreenLayout() {
        getWindow().setDecorFitsSystemWindows(false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableFullscreenLayout();
        }
    }
}

