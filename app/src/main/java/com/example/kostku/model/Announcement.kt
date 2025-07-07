package com.example.kostku.model

import com.google.firebase.Timestamp

data class Announcement(
    val message: String = "",
    val timestamp: Timestamp? = null,
    val id: String = ""
) 