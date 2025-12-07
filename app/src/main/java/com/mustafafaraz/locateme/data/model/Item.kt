package com.mustafafaraz.locateme.data.model

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
    val userEmail: String,
    val userProfilePic: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateItemRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val itemImages: List<String>? = null // base64 encoded images (no data URI prefix)
)