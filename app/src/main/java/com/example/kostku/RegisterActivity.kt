package com.example.kostku

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kostku.databinding.ActivityRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInput(name, email, password)) {
                registerUser(name, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Kembali ke halaman login
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Cek apakah email sudah terdaftar
                val existingUser = db.collection("user")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!existingUser.isEmpty) {
                    Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Buat data user baru
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )

                // Simpan ke Firebase
                db.collection("user")
                    .add(userData)
                    .await()

                Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman login
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration", e)
                Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return false
        }
        binding.tilName.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return false
        }
        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (password.length < 4) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        binding.tilPassword.error = null

        return true
    }
} 