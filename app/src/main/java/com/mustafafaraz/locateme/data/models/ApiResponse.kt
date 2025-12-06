package com.mustafafaraz.locateme.data.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String
)

data class AuthResponse(
    val token: String,
    val user: User
)