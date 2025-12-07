package com.mustafafaraz.locateme.data.local.dao

import androidx.room.*
import com.mustafafaraz.locateme.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesByChatId(chatId: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    suspend fun getMessagesByChatIdOnce(chatId: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: String, status: String)

    @Query("UPDATE messages SET id = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun updateWithServerId(localId: String, serverId: Int)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Int)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChat(chatId: Int)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
