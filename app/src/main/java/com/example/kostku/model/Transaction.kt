package com.example.kostku.model

import com.google.firebase.Timestamp

data class Transaction(
    val amount: Int = 0,
    val created_at: Timestamp? = null,
    val customer_email: String = "",
    val customer_name: String = "",
    val order_id: String = "",
    val room: Int = 0,
    val status: String = "",
    val token: String = ""
) 