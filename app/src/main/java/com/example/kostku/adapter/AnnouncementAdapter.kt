package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.databinding.ItemAnnouncementBinding
import com.example.kostku.model.Announcement

class AnnouncementAdapter(
    private val announcements: List<Announcement>,
    private val onDeleteClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement, onDeleteClick: (Announcement) -> Unit) {
            binding.tvMessage.text = announcement.message
            binding.tvTimestamp.text = formatDate(announcement.timestamp)
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(announcement)
            }
        }

        private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
            if (timestamp == null) return ""
            try {
                val date = timestamp.toDate()
                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                return formatter.format(date)
            } catch (e: Exception) {
                return ""
            }
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
        holder.bind(announcements[position], onDeleteClick)
    }

    override fun getItemCount() = announcements.size
} 