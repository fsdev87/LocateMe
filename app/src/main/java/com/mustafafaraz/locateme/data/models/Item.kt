package com.mustafafaraz.locateme.data.models

data class Item(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val status: String, // ACTIVE, RESOLVED, EXPIRED
    val userId: Int,
    val userName: String,
    val userProfilePic: String?,
    val createdAt: String
)