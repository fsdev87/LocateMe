package com.mustafafaraz.locateme.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.local.AppDatabase
import com.mustafafaraz.locateme.data.local.entity.MessageEntity
import com.mustafafaraz.locateme.data.local.entity.SyncQueueEntity
import com.mustafafaraz.locateme.data.model.ChatMessage
import com.mustafafaraz.locateme.data.model.SendMessageRequest
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.utils.TokenManager
import com.mustafafaraz.locateme.workers.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Repository for chat messages with offline-first support
 * Messages are saved locally first, then synced to server when online
 */
class MessageRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val messageDao = database.messageDao()
    private val syncQueueDao = database.syncQueueDao()
    private val tokenManager = TokenManager(context)
    private val workManager = WorkManager.getInstance(context)
    private val gson = Gson()

    companion object {
        private const val TAG = "MessageRepository"
    }

    /**
     * Get messages for a chat - Returns cached messages immediately
     */
    fun getMessagesByChatId(chatId: Int): Flow<List<ChatMessage>> {
        return messageDao.getMessagesByChatId(chatId).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    /**
     * Send a message - Works offline!
     * Saves to local DB immediately, queues for sync, shows in UI instantly
     */
    suspend fun sendMessage(
        chatId: Int,
        type: String,
        content: String? = null,
        messageImage: String? = null,
        currentUserId: Int
    ): Result<ChatMessage> {
        return try {
            val localId = "local_${UUID.randomUUID()}"
            val timestamp = System.currentTimeMillis()

            // Create the message request
            val request = SendMessageRequest(
                chatId = chatId,
                type = type,
                content = content,
                messageImage = messageImage
            )

            // Create local message entity (will be shown immediately in UI)
            val localMessage = MessageEntity(
                id = timestamp.toInt(), // Temporary ID
                chatId = chatId,
                senderId = currentUserId,
                receiverId = 0, // Will be updated from server
                type = type,
                content = content,
                mediaUrl = null,
                isRead = false,
                senderName = null,
                senderProfilePic = null,
                createdAt = java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.US
                ).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date(timestamp)),
                localId = localId,
                syncStatus = if (NetworkUtils.isOnline(context)) "SYNCING" else "PENDING",
                isSentByMe = true
            )

            // Save to local database immediately
            messageDao.insertMessage(localMessage)
            Log.d(TAG, "💾 Message saved locally with ID: $localId")

            // If online, try to send immediately
            if (NetworkUtils.isOnline(context)) {
                val token = tokenManager.getToken()
                if (!token.isNullOrEmpty()) {
                    try {
                        val authHeader = "Bearer $token"
                        val response = RetrofitClient.apiService.sendMessage(authHeader, request)

                        if (response.isSuccessful && response.body()?.success == true) {
                            val serverMessage = response.body()?.data!!

                            // Update with server data
                            messageDao.updateWithServerId(localId, serverMessage.id)

                            // Update full message details
                            val syncedMessage = MessageEntity.fromChatMessage(
                                serverMessage,
                                isSentByMe = true,
                                syncStatus = "SYNCED"
                            )
                            messageDao.insertMessage(syncedMessage)

                            Log.d(TAG, "✅ Message sent successfully: ${serverMessage.id}")
                            return Result.success(serverMessage)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to send immediately, will queue", e)
                    }
                }
            }

            // If offline OR immediate send failed, queue for background sync
            val syncOperation = SyncQueueEntity(
                operationType = "SEND_MESSAGE",
                entityType = "MESSAGE",
                payload = gson.toJson(request.copy(localId = localId)),
                status = "PENDING"
            )

            syncQueueDao.insert(syncOperation)
            Log.d(TAG, "📤 Message queued for sync")

            // Schedule background sync
            scheduleSync()

            // Return the local message
            Result.success(localMessage.toChatMessage())

        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Sync messages from server
     */
    suspend fun syncMessages(chatId: Int): Result<List<ChatMessage>> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getChatMessages(authHeader, chatId)

            if (response.isSuccessful && response.body()?.success == true) {
                val messages = response.body()?.data ?: emptyList()

                // Update cache
                val entities = messages.map {
                    MessageEntity.fromChatMessage(it, syncStatus = "SYNCED")
                }
                messageDao.insertMessages(entities)

                Log.d(TAG, "✅ Synced ${messages.size} messages")
                Result.success(messages)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to sync messages"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing messages", e)
            Result.failure(e)
        }
    }

    /**
     * Schedule background sync work
     */
    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Log.d(TAG, "🔄 Background sync scheduled")
    }

    /**
     * Force sync now (called when app detects internet connection)
     */
    fun triggerSync() {
        scheduleSync()
    }

    /**
     * Delete message from server and local cache
     */
    suspend fun deleteMessage(messageId: Int): Result<Unit> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection. Cannot delete message."))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.deleteMessage(authHeader, messageId)

            if (response.isSuccessful && response.body()?.success == true) {
                // Delete from local cache
                messageDao.deleteMessage(messageId)
                Log.d(TAG, "✅ Message deleted from server and cache: $messageId")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete message"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message", e)
            Result.failure(e)
        }
    }

    /**
     * Clear message cache
     */
    suspend fun clearCache(chatId: Int? = null) {
        if (chatId != null) {
            messageDao.deleteMessagesByChat(chatId)
        } else {
            messageDao.deleteAllMessages()
        }
        Log.d(TAG, "✅ Message cache cleared")
    }
}

// Extension to add localId to request
private data class SendMessageRequestWithLocal(
    val chatId: Int,
    val type: String,
    val content: String? = null,
    val messageImage: String? = null,
    val localId: String? = null
)

private fun SendMessageRequest.copy(localId: String) = SendMessageRequestWithLocal(
    chatId = chatId,
    type = type,
    content = content,
    messageImage = messageImage,
    localId = localId
)
