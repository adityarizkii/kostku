package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.databinding.ItemUserBinding
import com.example.kostku.model.UserTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UserAdapter(
    private val userTransactions: List<UserTransaction>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userTransaction: UserTransaction) {
            binding.apply {
                tvUserName.text = userTransaction.user.name
                tvUserEmail.text = userTransaction.user.email
                tvUserPassword.text = "••••••••" // Hide password for security
                tvPaymentAmount.text = formatPrice(userTransaction.transaction.amount)
                tvPaymentDate.text = formatDate(userTransaction.transaction.created_at)
                tvOrderId.text = "Order ID: ${userTransaction.transaction.order_id}"
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
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userTransactions[position])
    }

    override fun getItemCount() = userTransactions.size
} 