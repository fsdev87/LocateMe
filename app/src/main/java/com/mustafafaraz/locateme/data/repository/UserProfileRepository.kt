package com.mustafafaraz.locateme.data.repository

import android.content.Context
import android.util.Log
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.local.AppDatabase
import com.mustafafaraz.locateme.data.local.entity.UserProfileEntity
import com.mustafafaraz.locateme.data.model.User
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for user profile with offline support
 * API-first strategy: Try API first, fallback to cache if offline
 */
class UserProfileRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val userProfileDao = database.userProfileDao()
    private val tokenManager = TokenManager(context)

    companion object {
        private const val TAG = "UserProfileRepository"
    }

    /**
     * Get user profile - Flow for reactive updates
     */
    fun getUserProfile(): Flow<User?> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toUser()
        }
    }

    /**
     * Get user profile once (non-Flow)
     */
    suspend fun getUserProfileOnce(): User? {
        return userProfileDao.getUserProfileOnce()?.toUser()
    }

    /**
     * Fetch profile from API and update cache
     * API-first: Returns fresh data from server, updates cache
     */
    suspend fun syncProfile(): Result<User> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getProfile(authHeader)

            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.data!!

                // Update cache
                val entity = UserProfileEntity.fromUser(user)
                userProfileDao.insertUserProfile(entity)

                Log.d(TAG, "✅ Profile synced and cached")
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile", e)
            Result.failure(e)
        }
    }

    /**
     * Clear profile cache (on logout)
     */
    suspend fun clearCache() {
        userProfileDao.deleteUserProfile()
        Log.d(TAG, "✅ Profile cache cleared")
    }
}

