package com.example.kostku.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.R
import com.example.kostku.databinding.ItemMyRoomBinding
import com.example.kostku.model.Transaction
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder

class MyRoomAdapter(
    private val rooms: List<Transaction>
) : RecyclerView.Adapter<MyRoomAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemMyRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.apply {
                tvRoomNumber.text = "Room ${transaction.room}"
                tvStatus.text = transaction.status
                
                // Set background color based on status
                val statusColor = when (transaction.status.lowercase()) {
                    "settlement" -> R.color.status_settlement
                    "pending" -> R.color.status_pending
                    else -> R.color.status_pending
                }
                tvStatus.setBackgroundColor(ContextCompat.getColor(root.context, statusColor))

                // Set click listener for pending transactions
                root.setOnClickListener {
                    if (transaction.status.lowercase() == "pending" && transaction.token.isNotEmpty()) {
                        startMidtransPayment(transaction)
                    }
                }
            }
        }

        private fun startMidtransPayment(transaction: Transaction) {
            val context = binding.root.context
            
            try {
                // Initialize Midtrans SDK
                SdkUIFlowBuilder.init()
                    .setClientKey("SB-Mid-client-Kz7YBCRafj0Qab0U")
                    .setContext(context)
                    .setTransactionFinishedCallback { result ->
                        // Log transaction result
                        android.util.Log.d("MidtransDebug", "Transaction Result: ${result.status}")
                        android.util.Log.d("MidtransDebug", "Result Data: $result")

                        when (result.status) {
                            TransactionResult.STATUS_SUCCESS -> {
                                Toast.makeText(context, "Payment Success", Toast.LENGTH_LONG).show()
                            }
                            TransactionResult.STATUS_PENDING -> {
                                Toast.makeText(context, "Payment Pending", Toast.LENGTH_LONG).show()
                            }
                            TransactionResult.STATUS_FAILED -> {
                                Toast.makeText(context, "Payment Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setMerchantBaseUrl("https://midtrans-endpoint.vercel.app/")
                    .enableLog(true)
                    .buildSDK()

                // Start payment with stored token
                MidtransSDK.getInstance().startPaymentUiFlow(context, transaction.token)
            } catch (e: Exception) {
                android.util.Log.e("MidtransDebug", "Error starting payment: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount() = rooms.size
} 