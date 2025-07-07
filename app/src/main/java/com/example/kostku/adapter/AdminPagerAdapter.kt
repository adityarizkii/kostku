package com.example.kostku.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kostku.AdminAnnouncementFragment
import com.example.kostku.AdminPaymentFragment
import com.example.kostku.AdminUserFragment

class AdminPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminAnnouncementFragment()
            1 -> AdminPaymentFragment()
            2 -> AdminUserFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 