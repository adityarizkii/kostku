package com.example.kostku

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kostku.adapter.RoomTypeAdapter
import com.example.kostku.data.UserPreferencesManager
import com.example.kostku.model.RoomType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UserPreferencesManager
        userPreferencesManager = UserPreferencesManager(this)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up RecyclerView with room types
        val roomTypes = listOf(
            RoomType("A", 3000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam"),
            RoomType("B", 2000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam"),
            RoomType("C", 1000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam")
        )

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvKostList).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = RoomTypeAdapter(roomTypes) { roomType ->
                // Navigate to room type detail
                startActivity(RoomTypeDetailActivity.createIntent(this@HomeActivity, roomType))
            }
        }

        // Set up FAB
        findViewById<FloatingActionButton>(R.id.fabAddKost).setOnClickListener {
            // TODO: Implement add kost functionality
            Snackbar.make(it, "Add Kost feature coming soon!", Snackbar.LENGTH_SHORT).show()
        }

        // Load user data
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val userPreferences = userPreferencesManager.userPreferencesFlow.first()
            findViewById<android.widget.TextView>(R.id.tvWelcome).text = 
                "Welcome, ${userPreferences.name}"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {


            R.id.action_logout -> {
                lifecycleScope.launch {
                    userPreferencesManager.clearUserPreferences()
                    // Navigate to login screen
                    val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 