package com.mustafafaraz.locateme.data.model

data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String
)