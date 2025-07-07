package com.example.kostku

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kostku.adapter.UserAdapter
import com.example.kostku.databinding.FragmentAdminUserBinding
import com.example.kostku.model.User
import com.example.kostku.model.UserTransaction
import com.example.kostku.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminUserFragment : Fragment() {
    private var _binding: FragmentAdminUserBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AdminUserFragment"
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPaidUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList())
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun loadPaidUsers() {
        lifecycleScope.launch {
            try {
                // Get all settlement transactions
                val transactionSnapshot = db.collection("transaction")
                    .whereEqualTo("status", "settlement")
                    .get()
                    .await()

                // Get user data for each transaction
                val userTransactions = mutableListOf<UserTransaction>()
                for (doc in transactionSnapshot.documents) {
                    val transaction = doc.toObject(Transaction::class.java)
                    val customerEmail = doc.getString("customer_email")
                    
                    if (transaction != null && customerEmail != null) {
                        // Get user data for this customer
                        val userSnapshot = db.collection("user")
                            .whereEqualTo("email", customerEmail)
                            .get()
                            .await()

                        userSnapshot.documents.firstOrNull()?.let { userDoc ->
                            val user = userDoc.toObject(User::class.java)
                            user?.let { 
                                userTransactions.add(UserTransaction(user, transaction))
                            }
                        }
                    }
                }

                userAdapter = UserAdapter(userTransactions)
                binding.rvUsers.adapter = userAdapter

                if (userTransactions.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvUsers.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvUsers.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading paid users", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 