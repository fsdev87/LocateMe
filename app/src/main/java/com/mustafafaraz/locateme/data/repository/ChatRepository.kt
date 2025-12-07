package com.mustafafaraz.locateme.data.repository

import android.content.Context
import android.util.Log
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.local.AppDatabase
import com.mustafafaraz.locateme.data.local.entity.ChatEntity
import com.mustafafaraz.locateme.data.model.Chat
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for chats with offline support
 * API-first strategy: Try API first, fallback to cache if offline
 */
class ChatRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val chatDao = database.chatDao()
    private val tokenManager = TokenManager(context)

    companion object {
        private const val TAG = "ChatRepository"
    }

    /**
     * Get all chats - Flow for reactive updates
     */
    fun getAllChats(): Flow<List<Chat>> {
        return chatDao.getAllChats().map { entities ->
            entities.map { it.toChat() }
        }
    }

    /**
     * Get all chats once (non-Flow)
     */
    suspend fun getAllChatsOnce(): List<Chat> {
        return chatDao.getAllChatsOnce().map { it.toChat() }
    }

    /**
     * Get chat by ID from cache
     * Returns cached chat details for offline support
     */
    suspend fun getChatById(chatId: Int): Chat? {
        return chatDao.getChatById(chatId)?.toChat()
    }

    /**
     * Sync chats from server and update cache
     * API-first: Returns fresh data from server, updates cache
     */
    suspend fun syncChats(): Result<List<Chat>> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getUserChats(authHeader)

            if (response.isSuccessful && response.body()?.success == true) {
                val chats = response.body()?.data ?: emptyList()

                // Clear old chats and insert fresh ones
                chatDao.deleteAllChats()
                val entities = chats.map { ChatEntity.fromChat(it) }
                chatDao.insertChats(entities)

                Log.d(TAG, "✅ Synced ${chats.size} chats to cache")
                Result.success(chats)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch chats"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing chats", e)
            Result.failure(e)
        }
    }

    /**
     * Clear chat cache (on logout)
     */
    suspend fun clearCache() {
        chatDao.deleteAllChats()
        Log.d(TAG, "✅ Chat cache cleared")
    }
}
