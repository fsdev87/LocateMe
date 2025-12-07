package com.mustafafaraz.locateme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mustafafaraz.locateme.data.model.ChatMessage

/**
 * Local cache for chat messages with offline support
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: Int,
    val chatId: Int,
    val senderId: Int,
    val receiverId: Int,
    val type: String, // TEXT or IMAGE
    val content: String?,
    val mediaUrl: String?,
    val isRead: Boolean = false,
    val senderName: String?,
    val senderProfilePic: String?,
    val createdAt: String,

    // Offline sync fields
    val localId: String? = null, // Temporary ID for offline messages
    val syncStatus: String = "SYNCED", // PENDING, SYNCING, SYNCED, FAILED
    val isSentByMe: Boolean = false,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            type = type,
            content = content,
            _mediaUrl = mediaUrl,
            _isRead = if (isRead) 1 else 0,
            createdAt = createdAt,
            senderName = senderName,
            _senderProfilePic = senderProfilePic
        )
    }

    companion object {
        fun fromChatMessage(
            message: ChatMessage,
            isSentByMe: Boolean = false,
            syncStatus: String = "SYNCED",
            localId: String? = null
        ): MessageEntity {
            return MessageEntity(
                id = message.id,
                chatId = message.chatId,
                senderId = message.senderId,
                receiverId = message.receiverId,
                type = message.type,
                content = message.content,
                mediaUrl = message.mediaUrl, // Uses the getter (removes :5000)
                isRead = message.isRead, // Uses the getter (converts 1 to true)
                senderName = message.senderName,
                senderProfilePic = message.senderProfilePic, // Uses the getter (removes :5000)
                createdAt = message.createdAt,
                localId = localId,
                syncStatus = syncStatus,
                isSentByMe = isSentByMe
            )
        }
    }
}
