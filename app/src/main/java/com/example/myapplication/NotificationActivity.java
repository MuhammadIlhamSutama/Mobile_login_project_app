package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerViewHistory;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        // Animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        recyclerViewHistory.setLayoutAnimation(animation);

        // Load history from SharedPreferences
        SharedPreferences loginPref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userId = loginPref.getString("user_id", "");

        SharedPreferences historyPref = getSharedPreferences("password_history", MODE_PRIVATE);
        String historyKey = "history_" + userId;
        String historyRaw = historyPref.getString(historyKey, "");

        List<String> historyList = new ArrayList<>();
        if (!historyRaw.isEmpty()) {
            String[] items = historyRaw.split("\n");
            for (int i = items.length - 1; i >= 0; i--) {
                String item = items[i].trim();
                if (!item.isEmpty()) {
                    historyList.add(item);
                }
            }
        }

        // Hide RecyclerView if history is empty
        recyclerViewHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);

        // Setup adapter
        adapter = new NotificationAdapter(historyList);
        recyclerViewHistory.setAdapter(adapter);

        // Swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String removed = historyList.get(position);
                historyList.remove(position);
                adapter.notifyItemRemoved(position);

                Toast.makeText(NotificationActivity.this, "Deleted: " + removed, Toast.LENGTH_SHORT).show();

                // Update SharedPreferences with correct key
                StringBuilder newHistory = new StringBuilder();
                for (String item : historyList) {
                    newHistory.append(item).append("\n");
                }

                SharedPreferences.Editor editor = historyPref.edit();
                if (historyList.isEmpty()) {
                    // Remove key if no history left
                    editor.remove(historyKey);
                    recyclerViewHistory.setVisibility(View.GONE);
                } else {
                    editor.putString(historyKey, newHistory.toString().trim());
                }
                editor.apply();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerViewHistory);
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
