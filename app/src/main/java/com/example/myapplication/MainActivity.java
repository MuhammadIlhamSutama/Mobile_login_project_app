package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private List<Attendance> attendanceList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_CLIENTS_URL = BuildConfig.SUPABASE_CLIENTS_URL;
    private static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textName = findViewById(R.id.textName);
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String username = pref.getString("username", "User");
        textName.setText(username);

        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("login_pref", MODE_PRIVATE);
            sharedPref.edit().clear().apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        LinearLayout moveAbout = findViewById(R.id.move_about);
        moveAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));

        LinearLayout moveChangePassword = findViewById(R.id.move_change_password);
        moveChangePassword.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class)));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchAttendanceData);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setRefreshing(true);
        fetchAttendanceData();
        loadProfilePicture();
    }

    private void fetchAttendanceData() {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userId = pref.getString("user_id", null);

        if (userId == null) {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        String urlWithFilter = SUPABASE_URL + "?employee_id=eq." + userId;

        Request request = new Request.Builder()
                .url(urlWithFilter)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errBody = response.body().string();
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "HTTP " + response.code() + ": " + errBody, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    List<Attendance> items = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        items.add(new Attendance(
                                item.getInt("id"),
                                item.getString("date"),
                                item.getString("check_in"),
                                item.getString("check_out"),
                                item.getString("status")
                        ));
                    }

                    runOnUiThread(() -> {
                        attendanceList.clear();
                        for (int i = items.size() - 1; i >= 0; i--) {
                            attendanceList.add(items.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadProfilePicture() {
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userId = pref.getString("user_id", null);
        if (userId == null) return;

        OkHttpClient client = new OkHttpClient();
        String url = SUPABASE_CLIENTS_URL + "?user_id=eq." + userId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Silent failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String result = response.body().string();
                try {
                    JSONArray arr = new JSONArray(result);
                    if (arr.length() > 0) {
                        String imageUrl = arr.getJSONObject(0).optString("profile_picture", null);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            runOnUiThread(() -> {
                                ImageView imageView = findViewById(R.id.profileImageView);
                                Glide.with(MainActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_profile)
                                        .circleCrop()
                                        .into(imageView);
                                imageView.setOnClickListener(v -> {
                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                });
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
