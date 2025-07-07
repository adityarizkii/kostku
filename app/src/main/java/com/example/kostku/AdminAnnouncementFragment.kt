package com.example.kostku

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kostku.adapter.AnnouncementAdapter
import com.example.kostku.databinding.FragmentAdminAnnouncementBinding
import com.example.kostku.model.Announcement
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminAnnouncementFragment : Fragment() {
    private var _binding: FragmentAdminAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AdminAnnouncementFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAnnouncementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupRecyclerView()
        fetchAnnouncements()
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

                Toast.makeText(requireContext(), "Announcement posted successfully", Toast.LENGTH_SHORT).show()
                binding.etMessage.text?.clear()
                binding.tilMessage.error = null
                
                // Refresh the announcement list
                fetchAnnouncements()
            } catch (e: Exception) {
                Log.e(TAG, "Error posting announcement", e)
                Toast.makeText(requireContext(), "Error posting announcement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvAnnouncement.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchAnnouncements() {
        lifecycleScope.launch {
            try {
                val snapshot = db.collection("announcement")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                val announcements = snapshot.documents.mapNotNull { doc ->
                    val message = doc.getString("message") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")
                    Announcement(message, timestamp, doc.id)
                }
                binding.rvAnnouncement.adapter = AnnouncementAdapter(announcements) { announcement ->
                    deleteAnnouncement(announcement)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching announcements", e)
            }
        }
    }

    private fun deleteAnnouncement(announcement: Announcement) {
        lifecycleScope.launch {
            try {
                db.collection("announcement")
                    .document(announcement.id)
                    .delete()
                    .await()

                Toast.makeText(requireContext(), "Announcement deleted successfully", Toast.LENGTH_SHORT).show()
                
                // Refresh the announcement list
                fetchAnnouncements()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting announcement", e)
                Toast.makeText(requireContext(), "Error deleting announcement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 