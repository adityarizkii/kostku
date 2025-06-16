package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.databinding.ItemAnnouncementBinding
import com.example.kostku.model.Announcement

class AnnouncementAdapter(
    private val announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.tvMessage.text = announcement.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(announcements[position])
    }

    override fun getItemCount() = announcements.size
} 