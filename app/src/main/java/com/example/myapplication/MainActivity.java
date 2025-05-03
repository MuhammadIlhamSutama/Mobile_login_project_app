package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private static final String SUPABASE_URL = "https://oteebvgtsvgrkfooinrv.supabase.co/rest/v1/attendance";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90ZWVidmd0c3Zncmtmb29pbnJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNDU1NzQsImV4cCI6MjA1ODYyMTU3NH0.IE1UReAZZk-9fbqi8SV3EF86Py703eoJVvpEBbzCBAo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fullscreen & Hide ActionBar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Tampilkan nama user dari SharedPreferences
        TextView textName = findViewById(R.id.textName);
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String username = pref.getString("username", "User");
        textName.setText(username);

        // Tombol Logout
        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("login_pref", MODE_PRIVATE);
            sharedPref.edit().clear().apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchAttendanceData);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapter);

        // Load data awal
        swipeRefreshLayout.setRefreshing(true);
        fetchAttendanceData();
    }

    private void fetchAttendanceData() {
        OkHttpClient client = new OkHttpClient();

        // Ambil user ID dari SharedPreferences
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userId = pref.getString("user_id", null);

        if (userId == null) {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Tambahkan filter berdasarkan user_id
        String urlWithFilter = SUPABASE_URL + "?employee_id=eq." + userId;

        Request request = new Request.Builder()
                .url(urlWithFilter)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
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
                        attendanceList.addAll(items);
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
}
