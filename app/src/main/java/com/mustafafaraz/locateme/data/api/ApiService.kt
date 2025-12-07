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

    @HTTP(method = "DELETE", path = "api/users/account", hasBody = true)
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body request: DeleteAccountRequest
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

    // Save/Unsave Item Endpoints
    @POST("api/items/save")
    suspend fun saveItem(
        @Header("Authorization") token: String,
        @Body request: SaveItemRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/items/save/{itemId}")
    suspend fun unsaveItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int
    ): Response<ApiResponse<Unit>>

    @GET("api/items/saved")
    suspend fun getSavedItems(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Item>>>

    // Chat Endpoints
    @GET("api/chats")
    suspend fun getUserChats(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Chat>>>

    @POST("api/chats")
    suspend fun createOrGetChat(
        @Header("Authorization") token: String,
        @Body request: CreateChatRequest
    ): Response<ApiResponse<Chat>>

    @POST("api/chats/from-item/{itemId}")
    suspend fun createChatFromItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int
    ): Response<ApiResponse<Chat>>

    @GET("api/chats/{id}")
    suspend fun getChatById(
        @Header("Authorization") token: String,
        @Path("id") chatId: Int
    ): Response<ApiResponse<Chat>>

    // Message Endpoints
    @GET("api/messages/chat/{chatId}")
    suspend fun getChatMessages(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: Int,
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0
    ): Response<ApiResponse<List<ChatMessage>>>

    @POST("api/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<ChatMessage>>

    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(
        @Header("Authorization") token: String,
        @Path("id") messageId: Int
    ): Response<ApiResponse<Unit>>

    @PUT("api/messages/chat/{chatId}/read")
    suspend fun markMessagesAsRead(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: Int
    ): Response<ApiResponse<Unit>>

    // FCM Token Endpoint
    @PUT("api/auth/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body request: UpdateFcmTokenRequest
    ): Response<ApiResponse<Unit>>
}