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
import com.example.kostku.adapter.MyRoomAdapter
import com.example.kostku.adapter.TransactionAdapter
import com.example.kostku.data.UserPreferences
import com.example.kostku.data.UserPreferencesManager
import com.example.kostku.databinding.FragmentProfileBinding
import com.example.kostku.model.Transaction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferencesManager: UserPreferencesManager
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private val baseUrl = "https://midtrans-endpoint.vercel.app/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferencesManager = UserPreferencesManager(requireContext())
        setupRecyclerViews()
        loadUserData()
        loadMyRoom()
        loadTransactions()
    }

    private fun setupRecyclerViews() {
        binding.rvMyRoom.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userPreferences = userPreferencesManager.userPreferencesFlow.first()
                binding.tvUserName.text = userPreferences.name
                binding.tvUserEmail.text = userPreferences.email
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMyRoom() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userPreferences = userPreferencesManager.userPreferencesFlow.first()
                val querySnapshot = db.collection("transaction")
                    .whereEqualTo("customer_email", userPreferences.email)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                val transactions = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Transaction(
                            amount = doc.getLong("amount")?.toInt() ?: 0,
                            created_at = doc.getTimestamp("created_at") ?: Timestamp.now(),
                            customer_email = doc.getString("customer_email") ?: "",
                            customer_name = doc.getString("customer_name") ?: "",
                            order_id = doc.getString("order_id") ?: "",
                            room = doc.getLong("room")?.toInt() ?: 0,
                            status = doc.getString("status") ?: "",
                            token = doc.getString("token") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                if (transactions.isNotEmpty()) {
                    binding.btnConfirmPayment.visibility = View.VISIBLE
                    binding.btnConfirmPayment.setOnClickListener {
                        checkPaymentStatus(transactions[0])
                    }
                } else {
                    binding.btnConfirmPayment.visibility = View.GONE
                }

                binding.rvMyRoom.adapter = MyRoomAdapter(transactions)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading room data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPaymentStatus(transaction: Transaction) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userPrefs = userPreferencesManager.userPreferencesFlow.first()
                val url = "${baseUrl}check/${transaction.order_id}?customer_email=${userPrefs.email}&room=${transaction.room}&customer_name=${userPrefs.name}"

                // Jalankan network call di I/O dispatcher
                val (code, body) = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                      .url(url)
                      .get()
                      .build()
                    client.newCall(request).execute().use { resp ->
                      resp.code to resp.body!!.string()
                    }
                }

                Log.d("MidtransDebug", "Response Code: $code")
                Log.d("MidtransDebug", "Response Body: $body")

                if (code in 200..299) {
                    Toast.makeText(context, "Payment status checked successfully", Toast.LENGTH_SHORT).show()
                    loadMyRoom()
                    loadTransactions()
                } else {
                    Toast.makeText(context, "Failed: $code – $body", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
              Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
              Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
  }
}


    private fun loadTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userPreferences = userPreferencesManager.userPreferencesFlow.first()
                val querySnapshot = db.collection("transaction")
                    .whereEqualTo("customer_email", userPreferences.email)
                    .get()
                    .await()

                val transactions = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Transaction(
                            amount = doc.getLong("amount")?.toInt() ?: 0,
                            created_at = doc.getTimestamp("created_at") ?: Timestamp.now(),
                            customer_email = doc.getString("customer_email") ?: "",
                            customer_name = doc.getString("customer_name") ?: "",
                            order_id = doc.getString("order_id") ?: "",
                            room = doc.getLong("room")?.toInt() ?: 0,
                            status = doc.getString("status") ?: "",
                            token = doc.getString("token") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.created_at }

                binding.rvTransactions.adapter = TransactionAdapter(transactions)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading transactions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
