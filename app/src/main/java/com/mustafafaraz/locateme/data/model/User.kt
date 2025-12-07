package com.mustafafaraz.locateme.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    @SerializedName("full_name")
    val fullName: String,
    val email: String,
    @SerializedName("student_id")
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String,
    @SerializedName("profile_pic")
    private val _profilePic: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val stats: UserStats? = null
) {
    // Remove :5000 port from profile pic URL
    val profilePic: String?
        get() = _profilePic?.replace(":5000", "")
}

data class UserStats(
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("resolved_items")
    val resolvedItems: Int,
    @SerializedName("success_rate")
    val successRate: Float
)

data class UpdateProfileRequest(
    val fullName: String,
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String,
    val profilePic: String? = null // base64 encoded image (optional, only if changed)
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
