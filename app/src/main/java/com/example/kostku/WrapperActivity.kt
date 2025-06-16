package com.example.kostku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kostku.HomeFragment
import com.example.kostku.ProfileFragment
import com.example.kostku.R
import com.example.kostku.databinding.ActivityWrapperBinding

class WrapperActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWrapperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWrapperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment
        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_announcement -> loadFragment(AnnouncementFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .commit()
    }
}
