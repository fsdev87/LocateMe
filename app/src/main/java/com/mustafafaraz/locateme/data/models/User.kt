package com.mustafafaraz.locateme.data.models

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String,
    val profilePic: String? = null
)