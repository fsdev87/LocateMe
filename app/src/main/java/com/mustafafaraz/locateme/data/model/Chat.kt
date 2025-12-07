package com.mustafafaraz.locateme.data.model

import com.mustafafaraz.locateme.R

data class Chat(
    val id: String,
    val userName: String,
    val userEmail: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val userAvatar: Int = R.drawable.ic_person
)