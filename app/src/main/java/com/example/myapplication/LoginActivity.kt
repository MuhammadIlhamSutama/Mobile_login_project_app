package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import network.SupabaseClient

class LoginActivity : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: ImageButton
    private lateinit var btnTogglePassword: ImageButton
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Full Screen dengan WindowInsetsController
        hideSystemBars()

        etId = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)

        btnLogin.setOnClickListener {
            val idText = etId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (idText.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ID dan Password wajib diisi", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password harus lebih dari 6 karakter", Toast.LENGTH_SHORT).show()
            } else {
                login(idText, password)
            }
        }

        btnTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                // Sembunyikan password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                // Tampilkan password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            }
            etPassword.setSelection(etPassword.text.length) // Pindahkan cursor ke akhir
            isPasswordVisible = !isPasswordVisible
        }
    }

    private fun login(id: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = SupabaseClient.retrofitService.getClientByUsername("eq.$id")

                if (response.isNotEmpty()) {
                    val client = response[0]

                    if (client.password == password) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showCustomDialog()
                    }
                } else {
                    showCustomDialog()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Gagal login: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun showCustomDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.cancel_toast_login, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(true)

        val btnClose = view.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun hideSystemBars() {
        window.setDecorFitsSystemWindows(false) // Agar layout benar-benar penuh

        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars() // Biar fullscreen tetap aktif walaupun user swipe
        }
    }
}
