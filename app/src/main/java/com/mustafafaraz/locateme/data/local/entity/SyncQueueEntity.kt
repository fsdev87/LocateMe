package com.mustafafaraz.locateme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Queued operations that need to be synced when internet is available
 * This enables offline functionality - users can perform actions offline
 * and they will be synced automatically when connection returns
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val operationType: String, // SEND_MESSAGE, CREATE_ITEM, UPDATE_ITEM, SAVE_ITEM, etc.
    val entityType: String, // MESSAGE, ITEM, USER_PROFILE, etc.
    val entityId: Int? = null, // Local entity ID (if applicable)
    val payload: String, // JSON payload to send to API

    val status: String = "PENDING", // PENDING, IN_PROGRESS, FAILED, COMPLETED
    val retryCount: Int = 0,
    val maxRetries: Int = 3,

    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null
)

