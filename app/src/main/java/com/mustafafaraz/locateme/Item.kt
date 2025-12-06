package com.mustafafaraz.locateme

data class Item(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "FOUND" or "LOST"
    val location: String,
    val createdAt: String,
    val status: String // "active", "resolved", "expired"
)

