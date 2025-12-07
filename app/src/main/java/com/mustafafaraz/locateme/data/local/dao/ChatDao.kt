package com.mustafafaraz.locateme.data.local.dao

import androidx.room.*
import com.mustafafaraz.locateme.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // Get all chats (reactive)
    @Query("SELECT * FROM chats ORDER BY lastMessageAt DESC, createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    // Get all chats once (one-time fetch)
    @Query("SELECT * FROM chats ORDER BY lastMessageAt DESC, createdAt DESC")
    suspend fun getAllChatsOnce(): List<ChatEntity>

    // Get chat by ID
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Int): ChatEntity?

    // Insert single chat
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    // Insert multiple chats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    // Update chat
    @Update
    suspend fun updateChat(chat: ChatEntity)

    // Delete chat by ID
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: Int)

    // Delete all chats (on logout)
    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    // Delete old chats (older than 30 days)
    @Query("DELETE FROM chats WHERE lastSyncedAt < :timestamp")
    suspend fun deleteOldChats(timestamp: Long)
}

