package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import network.SupabaseClient

class LoginActivity : AppCompatActivity() {
    private lateinit var etId: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etId = findViewById(R.id.etId)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val idText = etId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate fields
            if (idText.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ID dan Password wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    // Try parsing the ID as an integer
                    val id = idText.toInt()

                    // Validate password length
                    if (password.length < 6) {
                        Toast.makeText(this, "Password harus lebih dari 6 karakter", Toast.LENGTH_SHORT).show()
                    } else {
                        // Call the login function
                        login(id, password)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "ID harus berupa angka", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun login(id: Int, password: String) {
        lifecycleScope.launch {
            try {
                // Fetch client data from Supabase
                val response = SupabaseClient.retrofitService.getClientById("eq.$id")

                if (response.isNotEmpty()) {
                    val client = response[0]

                    // Check password
                    if (client.password == password) {
                        // If login is successful, navigate to MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "ID tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Show error if the login request fails
                Toast.makeText(this@LoginActivity, "Gagal login: ${e.message}", Toast.LENGTH_LONG).show()
                // Log the exception for debugging purposes
                e.printStackTrace()
            }
        }
    }
}
