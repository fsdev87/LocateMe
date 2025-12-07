package com.mustafafaraz.locateme.data.local.dao

import androidx.room.*
import com.mustafafaraz.locateme.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingOperationsFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'FAILED' AND retryCount < maxRetries ORDER BY createdAt ASC")
    suspend fun getFailedOperationsForRetry(): List<SyncQueueEntity>

    @Insert
    suspend fun insert(operation: SyncQueueEntity): Long

    @Update
    suspend fun update(operation: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sync_queue SET status = 'FAILED', retryCount = retryCount + 1, errorMessage = :error, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun markAsFailed(id: Long, error: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()
}
