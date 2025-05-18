package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textName, editTextClientId, rfidTag;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("login_pref", MODE_PRIVATE);
            sharedPref.edit().clear().apply();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        // Terapkan padding atas untuk hindari status bar / notch
        findViewById(R.id.profile).setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset = insets.getInsets(android.view.WindowInsets.Type.statusBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });
        findViewById(R.id.profile).requestApplyInsets();

        textName = findViewById(R.id.textName);
        editTextClientId = findViewById(R.id.editTextClientId);
        rfidTag = findViewById(R.id.rfid_tag);
        backButton = findViewById(R.id.back_button);

        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        String name = pref.getString("username", "N/A");
        String id = pref.getString("user_id", "N/A");
        String rfid = pref.getString("rfid_tag", "N/A");

        textName.setText(name);
        editTextClientId.setText("ID : " + id);
        rfidTag.setText("RFID : " + rfid);

        backButton.setOnClickListener(v -> onBackPressed());

        // --- Tambahan animasi ---
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Terapkan animasi
        findViewById(R.id.content_card).startAnimation(slideUp);
        textName.startAnimation(fadeIn);
        editTextClientId.startAnimation(fadeIn);
        rfidTag.startAnimation(fadeIn);
        backButton.startAnimation(fadeIn);
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
