package com.mustafafaraz.locateme.data.model

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val student_id: String,
    val batch: String,
    val department: String,
    val section: String,
    val profile_pic: String? = null,
    val created_at: String? = null
)