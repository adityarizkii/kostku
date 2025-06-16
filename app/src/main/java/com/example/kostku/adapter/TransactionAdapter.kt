package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.R
import com.example.kostku.databinding.ItemTransactionBinding
import com.example.kostku.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.apply {
                tvOrderId.text = transaction.order_id
                tvAmount.text = formatPrice(transaction.amount)
                tvStatus.text = transaction.status
                tvDate.text = formatDate(transaction.created_at)
                
                // Set background color based on status
                val statusColor = when (transaction.status.lowercase()) {
                    "settlement" -> R.color.status_settlement
                    "pending" -> R.color.status_pending
                    else -> R.color.status_pending
                }
                tvStatus.setBackgroundColor(ContextCompat.getColor(root.context, statusColor))
            }
        }

        private fun formatPrice(price: Int): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(price)
        }

        private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
            if (timestamp == null) return ""
            val date = timestamp.toDate()
            val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
            return formatter.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size
} 