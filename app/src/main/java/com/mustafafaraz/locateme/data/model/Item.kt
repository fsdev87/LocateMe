package com.mustafafaraz.locateme.data.model

import com.google.gson.annotations.SerializedName

data class Item(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("image_urls")
    private val _imageUrls: List<String>,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val status: String, // ACTIVE, RESOLVED, EXPIRED
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("user_student_id")
    val userStudentId: String?,
    @SerializedName("user_batch")
    val userBatch: String?,
    @SerializedName("user_department")
    val userDepartment: String?,
    @SerializedName("user_section")
    val userSection: String?,
    @SerializedName("user_profile_pic")
    private val _userProfilePic: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("is_saved")
    var isSaved: Boolean = false
) {
    // Remove :5000 port from image URLs (backend includes it incorrectly)
    val imageUrls: List<String>
        get() = _imageUrls.map { it.replace(":5000", "") }

    // Remove :5000 port from profile pic URL
    val userProfilePic: String?
        get() = _userProfilePic?.replace(":5000", "")
}

data class CreateItemRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val itemImages: List<String>? = null // base64 encoded images (no data URI prefix)
)

data class UpdateItemRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val status: String, // ACTIVE, RESOLVED, EXPIRED
    val itemImages: List<String>? = null // base64 encoded images (no data URI prefix)
)

data class SaveItemRequest(
    val itemId: Int
)
