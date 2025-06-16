package com.example.kostku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kostku.data.UserPreferencesManager
import com.example.kostku.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferencesManager: UserPreferencesManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserPreferencesManager
        userPreferencesManager = UserPreferencesManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if admin
            if (email == "admin@gmail.com") {
                // Attempt admin login
                lifecycleScope.launch {
                    try {
                        val usersSnapshot = db.collection("user")
                            .whereEqualTo("email", email)
                            .whereEqualTo("password", password)
                            .get()
                            .await()

                        if (!usersSnapshot.isEmpty) {
                            // Admin login successful
                            Toast.makeText(this@LoginActivity, "Admin login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during admin login", e)
                        Toast.makeText(this@LoginActivity, "Error during login: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                return@setOnClickListener
            }

            // Regular user login
            lifecycleScope.launch {
                try {
                    // Fetch all users from Firestore
                    val usersSnapshot = db.collection("user").get().await()
                    
                    // Check if user exists and password matches
                    val user = usersSnapshot.documents.find { doc ->
                        doc.getString("email") == email && doc.getString("password") == password
                    }

                    if (user != null) {
                        // Login successful
                        val userName = user.getString("name") ?: ""
                        
                        // Save user data to DataStore
                        userPreferencesManager.saveUserPreferences(email, userName)
                        
                        // Show success message
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        
                        // Navigate back to MainActivity
                        val intent = Intent(this@LoginActivity, WrapperActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during login", e)
                    Toast.makeText(this@LoginActivity, "Error during login: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}