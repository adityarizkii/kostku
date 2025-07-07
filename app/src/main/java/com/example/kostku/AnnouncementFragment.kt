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
import com.example.kostku.databinding.FragmentAnnouncementBinding
import com.example.kostku.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnnouncementFragment : Fragment() {
    private var _binding: FragmentAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAnnouncements()
    }

    private fun setupRecyclerView() {
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(context)
    }

    private fun loadAnnouncements() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("AnnouncementFragment", "Starting to load announcements...")
                
                val querySnapshot = db.collection("announcement")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                Log.d("AnnouncementFragment", "Query completed. Document count: ${querySnapshot.size()}")
                
                if (querySnapshot.isEmpty) {
                    Log.d("AnnouncementFragment", "No documents found in collection")
                    return@launch
                }

                // Log each document
                querySnapshot.documents.forEach { doc ->
                    Log.d("AnnouncementFragment", "Document ID: ${doc.id}")
                    Log.d("AnnouncementFragment", "Document data: ${doc.data}")
                }

                val announcements = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        val message = doc.getString("message")
                        val timestamp = doc.getTimestamp("timestamp")
                        Log.d("AnnouncementFragment", "Processing document ${doc.id}: message=$message")
                        
                        if (message == null) {
                            Log.w("AnnouncementFragment", "Message is null for document ${doc.id}")
                            return@mapNotNull null
                        }
                        
                        Announcement(message = message, timestamp = timestamp, id = doc.id)
                    } catch (e: Exception) {
                        Log.e("AnnouncementFragment", "Error parsing announcement ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                Log.d("AnnouncementFragment", "Successfully parsed ${announcements.size} announcements")
                binding.rvAnnouncements.adapter = AnnouncementAdapter(announcements) { announcement ->
                    // Do nothing for regular users - delete only available for admin
                }
            } catch (e: Exception) {
                Log.e("AnnouncementFragment", "Error loading announcements", e)
                Toast.makeText(context, "Error loading announcements: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 