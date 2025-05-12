package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;
import data.model.Employee;
import network.SupabaseClient;
import network.SupabaseService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

        private EditText etId, etPassword;
        private ImageButton btnLogin, btnTogglePassword;
        private boolean isPasswordVisible = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // ✅ Check if already logged in
            SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
            if (pref.contains("user_id")) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                return;
            }

            setContentView(R.layout.activity_login);

            etId = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            btnTogglePassword = findViewById(R.id.btnTogglePassword);

            Button move_signin = findViewById(R.id.move_register);
            move_signin.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });

            Button btnContactUs = findViewById(R.id.button_contact_us);
            btnContactUs.setOnClickListener(v -> openWhatsAppChat("6285123534372"));

            btnLogin.setOnClickListener(v -> {
                String idText = etId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                etId.clearFocus();
                etPassword.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                if (idText.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "ID dan Password wajib diisi", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(LoginActivity.this, "Password harus lebih dari 6 karakter", Toast.LENGTH_SHORT).show();
                } else {
                    login(idText, password);
                }
            });

            btnTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.ic_visibility);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                }
                etPassword.setSelection(etPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            });

            findViewById(R.id.main).setOnClickListener(v -> {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                }
            });
        }

        private void login(String name, String password) {
            SupabaseService service = SupabaseClient.INSTANCE.getRetrofitService();
            Call<List<Employee>> call = service.getEmployeeByName(
                    "eq." + name,
                    SupabaseClient.API_KEY,
                    "Bearer " + SupabaseClient.API_KEY,
                    "application/json"
            );

            call.enqueue(new Callback<List<Employee>>() {
                @Override
                public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Employee user = response.body().get(0);

                        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                        if (result.verified) {
                            SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
                            pref.edit()
                                    .putString("username", user.getName())
                                    .putString("user_id", String.valueOf(user.getId()))
                                    .putString("password", user.getPassword())
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            showCancelLoginDialog(); // wrong password
                        }
                    } else {
                        showCancelLoginDialog(); // user not found
                    }
                }

                @Override
                public void onFailure(Call<List<Employee>> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showCancelLoginDialog() {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.cancel_toast_login, null);
            dialog.setContentView(view);

            view.findViewById(R.id.btnClosed).setOnClickListener(v -> dialog.dismiss());

            dialog.show();

            // Auto dismiss after 2.5 seconds (optional)
            new android.os.Handler().postDelayed(dialog::dismiss, 2500);
        }

        private void openWhatsAppChat(String phoneNumber) {
            try {
                String url = "https://wa.me/" + phoneNumber;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(android.net.Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
            }
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