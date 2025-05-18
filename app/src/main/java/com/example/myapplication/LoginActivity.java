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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;
import data.model.Employee;
import network.SupabaseClient;
import network.SupabaseService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 🟢 Ganti dari AppCompatActivity ke BaseActivity
public class LoginActivity extends BaseActivity {

    private EditText etId, etPassword;
    private ImageButton btnLogin, btnTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
        if (pref.contains("user_id")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // 🟢 Inisialisasi loading overlay setelah setContentView
        setupLoadingOverlay();

        etId = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        TextView move_signin = findViewById(R.id.move_register);
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
            hideKeyboard();

            if (idText.isEmpty() || password.isEmpty()) {
                showCustomToast("ID dan Password wajib diisi");
            } else if (password.length() < 6) {
                showCustomToast("Password harus lebih dari 6 karakter");
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
                hideKeyboard();
                view.clearFocus();
            }
        });
    }

    private void login(String name, String password) {
        // 🟢 Tampilkan loading saat mulai login
        showLoading();

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
                // 🟢 Sembunyikan loading setelah respons
                hideLoading();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Employee user = response.body().get(0);

                    // Pindahkan BCrypt verifikasi ke background thread agar tidak blocking UI
                    new Thread(() -> {
                        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                        runOnUiThread(() -> {
                            if (result.verified) {
                                SharedPreferences pref = getSharedPreferences("login_pref", MODE_PRIVATE);
                                pref.edit()
                                        .putString("username", user.getName())
                                        .putString("user_id", String.valueOf(user.getId()))
                                        .putString("password", user.getPassword())
                                        .putString("rfid_tag", user.getRfid_tag())
                                        .apply();

                                hideKeyboard();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                new android.os.Handler().postDelayed(() -> finish(), 100);

                            } else {
                                showCustomToast("ID atau password salah");
                            }
                        });
                    }).start();

                } else {
                    showCustomToast("ID atau password salah");
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                hideLoading(); // 🟢 Sembunyikan juga saat error
                showCustomToast("Gagal koneksi jaringan");
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showCustomToast(String message) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.cancel_toast_login, null);
        dialog.setContentView(view);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }

        Button btnClosed = view.findViewById(R.id.btnClosed);
        btnClosed.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Auto dismiss after 2.5 seconds
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
