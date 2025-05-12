package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;
import network.SupabaseService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText editTextUserId, editTextOldPassword, editTextNewPassword, editTextConfirmPassword;
    Button buttonUpdate;

    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String API_KEY = BuildConfig.SUPABASE_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextUserId = findViewById(R.id.editTextClientId);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonUpdate = findViewById(R.id.buttonUpdatePassword);

        Button buttonBack = findViewById(R.id.buttonClose);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        String userId = sharedPref.getString("user_id", "");
        if (!userId.isEmpty()) {
            editTextUserId.setText(userId);
        }

        buttonUpdate.setOnClickListener(view -> {
            String oldPassword = editTextOldPassword.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (userId.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ChangePasswordActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(newPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Password must be 6-15 characters long and contain both letters and numbers", Toast.LENGTH_LONG).show();
                return;
            }

            verifyOldPassword(userId, oldPassword, newPassword);
        });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 15) return false;
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }

    private void verifyOldPassword(String userId, String oldPassword, String newPassword) {
        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        String storedHashedPassword = sharedPref.getString("password", "");

        BCrypt.Result result = BCrypt.verifyer().verify(oldPassword.toCharArray(), storedHashedPassword);
        if (result.verified) {
            updatePassword(userId, newPassword);
        } else {
            Toast.makeText(ChangePasswordActivity.this, "Old Password is incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword(String userId, String newPassword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseService service = retrofit.create(SupabaseService.class);

        String hashedNewPassword = hashPassword(newPassword);
        if (hashedNewPassword == null) {
            Toast.makeText(this, "Failed to hash new password", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("password", hashedNewPassword);

        Call<Void> call = service.updatePassword(
                API_KEY,
                "Bearer " + API_KEY,
                "application/json",
                "eq." + userId,
                body
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Save history
                    String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                    String event = "Password changed at " + timestamp;

                    SharedPreferences historyPref = getSharedPreferences("password_history", MODE_PRIVATE);
                    String history = historyPref.getString("history", "");
                    history += event + "\n";
                    historyPref.edit().putString("history", history).apply();

                    // Save new password locally
                    SharedPreferences sharedPref = getSharedPreferences("login_pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("password", hashedNewPassword);
                    editor.apply();

                    sendPasswordUpdateNotification();

                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e("Supabase", "Update failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Supabase", "Network error: " + t.getMessage());
            }
        });
    }

    private void sendPasswordUpdateNotification() {
        String channelId = "password_update_channel";
        String channelName = "Password Updates";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when password is updated");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_lock) // Make sure this icon exists
                .setContentTitle("Password Changed")
                .setContentText("Tap to view change history.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
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
