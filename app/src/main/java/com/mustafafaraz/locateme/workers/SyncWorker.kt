package com.mustafafaraz.locateme.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.local.AppDatabase
import com.mustafafaraz.locateme.data.model.SendMessageRequest
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.utils.TokenManager

/**
 * Background worker that syncs queued operations when internet is available
 * Automatically retries failed operations with exponential backoff
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_pending_operations"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 SyncWorker started")

        // Check internet connectivity
        if (!NetworkUtils.isOnline(applicationContext)) {
            Log.d(TAG, "❌ No internet connection, retrying later")
            return Result.retry()
        }

        val database = AppDatabase.getInstance(applicationContext)
        val syncQueueDao = database.syncQueueDao()
        val messageDao = database.messageDao()
        val tokenManager = TokenManager(applicationContext)

        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "❌ No auth token, cannot sync")
            return Result.failure()
        }

        val authHeader = "Bearer $token"

        // Get all pending operations
        val pendingOps = syncQueueDao.getPendingOperations() +
                         syncQueueDao.getFailedOperationsForRetry()

        if (pendingOps.isEmpty()) {
            Log.d(TAG, "✅ No pending operations")
            return Result.success()
        }

        Log.d(TAG, "📦 Found ${pendingOps.size} pending operations")

        var successCount = 0
        var failureCount = 0

        for (operation in pendingOps) {
            try {
                syncQueueDao.updateStatus(operation.id, "IN_PROGRESS")

                when (operation.operationType) {
                    "SEND_MESSAGE" -> {
                        val success = syncMessage(authHeader, operation, messageDao)
                        if (success) {
                            syncQueueDao.updateStatus(operation.id, "COMPLETED")
                            successCount++
                        } else {
                            syncQueueDao.markAsFailed(operation.id, "API call failed")
                            failureCount++
                        }
                    }
                    // Add more operation types here (CREATE_ITEM, UPDATE_ITEM, etc.)
                    else -> {
                        Log.w(TAG, "Unknown operation type: ${operation.operationType}")
                        syncQueueDao.updateStatus(operation.id, "COMPLETED")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error syncing operation ${operation.id}", e)
                syncQueueDao.markAsFailed(operation.id, e.message ?: "Unknown error")
                failureCount++
            }
        }

        // Clean up completed operations
        syncQueueDao.deleteCompleted()

        Log.d(TAG, "✅ Sync completed: $successCount success, $failureCount failed")

        return if (failureCount > 0) Result.retry() else Result.success()
    }

    private suspend fun syncMessage(
        authHeader: String,
        operation: com.mustafafaraz.locateme.data.local.entity.SyncQueueEntity,
        messageDao: com.mustafafaraz.locateme.data.local.dao.MessageDao
    ): Boolean {
        return try {
            val gson = Gson()
            val messageRequest = gson.fromJson(operation.payload, SendMessageRequest::class.java)

            val response = RetrofitClient.apiService.sendMessage(authHeader, messageRequest)

            if (response.isSuccessful && response.body()?.success == true) {
                val sentMessage = response.body()?.data

                if (sentMessage != null) {
                    // Update local message with server ID
                    val localId = operation.payload.substringAfter("\"localId\":\"").substringBefore("\"")
                    if (localId.isNotEmpty()) {
                        messageDao.updateWithServerId(localId, sentMessage.id)
                    }

                    Log.d(TAG, "✅ Message synced successfully: ${sentMessage.id}")
                    true
                } else {
                    false
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing message", e)
            false
        }
    }
}

