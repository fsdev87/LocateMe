package com.mustafafaraz.locateme

data class ChatMessage(
    val messageText: String,
    val type: String, // "sent" or "received"
    val timestamp: String
)

