package com.mustafafaraz.locateme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mustafafaraz.locateme.data.model.Chat

/**
 * Local cache for chats list
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: Int,
    val otherUserId: Int,
    val otherUserName: String,
    val otherUserEmail: String,
    val userProfilePic: String?,
    val lastMessage: String?,
    val lastMessageType: String?,
    val lastMessageTime: String?,
    val createdAt: String,
    val lastMessageAt: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toChat(): Chat {
        return Chat(
            id = id,
            otherUserId = otherUserId,
            otherUserName = otherUserName,
            otherUserEmail = otherUserEmail,
            _userProfilePic = userProfilePic,
            lastMessage = lastMessage,
            lastMessageType = lastMessageType,
            lastMessageTime = lastMessageTime,
            createdAt = createdAt,
            lastMessageAt = lastMessageAt
        )
    }

    companion object {
        fun fromChat(chat: Chat): ChatEntity {
            return ChatEntity(
                id = chat.id,
                otherUserId = chat.otherUserId,
                otherUserName = chat.otherUserName,
                otherUserEmail = chat.otherUserEmail,
                userProfilePic = chat.userProfilePic,
                lastMessage = chat.lastMessage,
                lastMessageType = chat.lastMessageType,
                lastMessageTime = chat.lastMessageTime,
                createdAt = chat.createdAt,
                lastMessageAt = chat.lastMessageAt
            )
        }
    }
}

