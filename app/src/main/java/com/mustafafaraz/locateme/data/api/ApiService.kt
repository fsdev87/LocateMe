package com.mustafafaraz.locateme.data.api

import com.mustafafaraz.locateme.data.model.*
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

    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<User>>

    @PUT("api/users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/users/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

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

    @GET("api/items/{id}")
    suspend fun getItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int
    ): Response<ApiResponse<Item>>

    @GET("api/items/my-items")
    suspend fun getMyItems(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Item>>>

    @PUT("api/items/{id}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int,
        @Body request: UpdateItemRequest
    ): Response<ApiResponse<Item>>

    @DELETE("api/items/{id}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int
    ): Response<ApiResponse<Unit>>
}