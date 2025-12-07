package com.mustafafaraz.locateme.data.api

import com.mustafafaraz.locateme.data.model.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication Endpoints
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    // User Profile Endpoints
    @GET("api/users/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<User>>

    // Item Endpoints
    @POST("api/items")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Body request: CreateItemRequest
    ): Response<ApiResponse<Item>>

    @GET("api/items")
    suspend fun getItems(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<Item>>>
}