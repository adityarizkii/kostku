package com.example.kostku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kostku.databinding.ActivityAdminBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AdminActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPost.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isNotEmpty()) {
                postAnnouncement(message)
            } else {
                binding.tilMessage.error = "Message is required"
            }
        }
    }

    private fun postAnnouncement(message: String) {
        lifecycleScope.launch {
            try {
                val announcement = hashMapOf(
                    "message" to message,
                    "timestamp" to Timestamp.now()
                )

                db.collection("announcement")
                    .add(announcement)
                    .await()

                Toast.makeText(this@AdminActivity, "Announcement posted successfully", Toast.LENGTH_SHORT).show()
                binding.etMessage.text?.clear()
                binding.tilMessage.error = null
            } catch (e: Exception) {
                Log.e(TAG, "Error posting announcement", e)
                Toast.makeText(this@AdminActivity, "Error posting announcement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 