package com.example.myapplication;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText inputEmail, inputName, inputPassword, inputRfid;
    ImageButton btnRegister;

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_CLIENTS_URL;
    private static final String SUPABASE_AUTH_URL = BuildConfig.SIGNUP_URL;
    private static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.input_email);
        inputName = findViewById(R.id.input_name);
        inputPassword = findViewById(R.id.input_password);
        inputRfid = findViewById(R.id.input_rfid);
        btnRegister = findViewById(R.id.btn_register);

        // Get available RFID tag
        getRfidTag();

        btnRegister.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String rfid = inputRfid.getText().toString().trim();

            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || rfid.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                registerWithEmail(email, password, name, rfid);
            }
        });

        ImageView imageView = findViewById(R.id.image_card);
        Glide.with(this).asGif().load(R.drawable.card_gif).into(imageView);

    }

    private void getRfidTag() {
        String url = "https://oteebvgtsvgrkfooinrv.supabase.co/rest/v1/rfid_tag?status=eq.available&order=id.asc&limit=1";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Error fetching RFID", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Failed to get RFID data", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONArray result = new JSONArray(responseBody);

                    if (result.length() == 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "No available RFID tags", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    JSONObject rfidData = result.getJSONObject(0);
                    String rfid = rfidData.getString("rfid_tag");

                    runOnUiThread(() -> inputRfid.setText(rfid));

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Error parsing RFID data", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void registerWithEmail(String email, String password, String name, String rfid) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_AUTH_URL)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Auth failed", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Signup failed: " + responseBody, Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    JSONObject respJson = new JSONObject(responseBody);
                    String uuid = respJson.getJSONObject("user").getString("id");

                    String hashedPassword = hashPassword(password);
                    insertEmployee(name, hashedPassword, rfid, uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void insertEmployee(String name, String password, String rfid, String uuid) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("password", password);
            json.put("rfid_tag", rfid);
            json.put("uuid", uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Insert failed", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Update RFID status
                        updateRfidStatus(rfid);

                        Toast.makeText(RegisterActivity.this, "Register success!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Insert failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateRfidStatus(String rfid) {
        String updateUrl = BuildConfig.SUPABASE_UPDATE_RFID_URL + rfid;

        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("status", "assigned");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody updateBody = RequestBody.create(updateJson.toString(), JSON);
        Request updateRequest = new Request.Builder()
                .url(updateUrl)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .method("PATCH", updateBody)
                .build();

        client.newCall(updateRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Optional: log error
            }

            @Override
            public void onResponse(Call call, Response response) {
                // Optional: log success/failure
            }
        });
    }

    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
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
