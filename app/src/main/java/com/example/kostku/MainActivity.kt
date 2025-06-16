package com.example.kostku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Set up login button click listener
        findViewById<MaterialButton>(R.id.btnGoToLogin).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Fetch users from Firestore
        db.collection("user")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    Log.d(TAG, "User: $name | $email")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching users", e)
            }
    }
}
