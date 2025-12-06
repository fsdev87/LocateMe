package com.mustafafaraz.locateme.data.api

import com.mustafafaraz.locateme.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth Endpoints
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @PUT("api/auth/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body fcmToken: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // User Endpoints
    @GET("api/users/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<User>>

    @Multipart
    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("fullName") fullName: RequestBody? = null,
        @Part("batch") batch: RequestBody? = null,
        @Part("department") department: RequestBody? = null,
        @Part("section") section: RequestBody? = null,
        @Part profilePic: MultipartBody.Part? = null
    ): Response<ApiResponse<User>>

    // Item Endpoints
    @GET("api/items")
    suspend fun getItems(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<Item>>>

    @Multipart
    @POST("api/items")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("location") location: RequestBody,
        @Part("type") type: RequestBody,
        @Part itemImages: List<MultipartBody.Part>
    ): Response<ApiResponse<Item>>

    @GET("api/items/{id}")
    suspend fun getItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int
    ): Response<ApiResponse<Item>>

    @Multipart
    @PUT("api/items/{id}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int,
        @Part("title") title: RequestBody? = null,
        @Part("status") status: RequestBody? = null
    ): Response<ApiResponse<Item>>

    @DELETE("api/items/{id}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: Int
    ): Response<ApiResponse<Unit>>

    // Save Item
    @POST("api/items/save")
    suspend fun saveItem(
        @Header("Authorization") token: String,
        @Body itemId: Map<String, Int>
    ): Response<ApiResponse<Unit>>

    @GET("api/items/saved")
    suspend fun getSavedItems(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Item>>>
}