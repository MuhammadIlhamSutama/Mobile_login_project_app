package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowInsetsController;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

import data.model.Foto;
import network.FotoService;
import network.SupabaseClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    private final int PAGE_SIZE = 9;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Menggunakan TreeMap dengan reverseOrder agar tanggal terbaru muncul dulu
    private final Map<String, List<Foto>> allGroupedMap = new TreeMap<>(Collections.reverseOrder());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerViewHistory);

        int numberOfColumns = calculateNoOfColumns(180);
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new HistoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadAllFotos();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (!rv.canScrollVertically(1) && !isLoading && !isLastPage) {
                    loadNextPage();
                }
            }
        });
    }

    private void loadAllFotos() {
        isLoading = true;

        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) {
            Log.e("HistoryActivity", "Username not found in SharedPreferences");
            isLoading = false;
            return;
        }

        FotoService apiService = SupabaseClient.INSTANCE.getFotoService();
        apiService.getFotosByName(
                "*",                          // select semua kolom
                "tanggal.desc,jam.asc",       // order tanggal desc, jam asc
                "eq." + username              // filter berdasarkan username
        ).enqueue(new Callback<List<Foto>>() {
            @Override
            public void onResponse(Call<List<Foto>> call, Response<List<Foto>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    // Group by tanggal
                    for (Foto foto : response.body()) {
                        allGroupedMap.computeIfAbsent(foto.tanggal, k -> new ArrayList<>()).add(foto);
                    }
                    // Sort tiap grup by jam ascending (sebenarnya sudah diorder, tapi untuk aman)
                    for (List<Foto> list : allGroupedMap.values()) {
                        list.sort(Comparator.comparing(f -> f.jam));
                    }
                    loadNextPage();
                } else {
                    Log.e("HistoryActivity", "Failed to get data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Foto>> call, Throwable t) {
                isLoading = false;
                Log.e("HistoryActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void loadNextPage() {
        isLoading = true;

        List<Map.Entry<String, List<Foto>>> allEntries = new ArrayList<>(allGroupedMap.entrySet());
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allEntries.size());

        if (start >= end) {
            isLastPage = true;
            isLoading = false;
            return;
        }

        List<Map.Entry<String, List<Foto>>> pageData = allEntries.subList(start, end);
        adapter.addData(pageData);

        currentPage++;
        isLoading = false;
    }

    private int calculateNoOfColumns(float columnWidthDp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return Math.max(1, (int) (screenWidthDp / columnWidthDp));
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
