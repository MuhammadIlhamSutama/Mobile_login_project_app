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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

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
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);


        Button btnContactUs = findViewById(R.id.button_contact_us);
        btnContactUs.setOnClickListener(v -> openWhatsAppChat("6285123534372")); // Replace with your admin's number

        btnLogin.setOnClickListener(v -> {
            String idText = etId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

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
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void login(String id, String password) {
        SupabaseService service = SupabaseClient.INSTANCE.getRetrofitService();
        Call<List<Employee>> call = service.getEmployeeByName(
                "eq." + id,
                SupabaseClient.API_KEY,
                "Bearer " + SupabaseClient.API_KEY,
                "application/json"
        );

        call.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Employee employee = response.body().get(0);
                    if (employee.getPassword().equals(password)) {
                        // Simpan data user ke SharedPreferences
                        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("user_id", String.valueOf(employee.getId()));
                        editor.putString("username", employee.getName());
                        editor.putString("access_token", employee.getAccess_token());
                        editor.putString("password", password); // simpan password untuk validasi di ChangePasswordActivity
                        editor.apply();

                        // Pindah ke MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showCustomDialog();
                    }
                } else {
                    showCustomDialog();
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Gagal login: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void showCustomDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.cancel_toast_login, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);

        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void openWhatsAppChat(String phoneNumber) {
        String url = "https://wa.me/" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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
