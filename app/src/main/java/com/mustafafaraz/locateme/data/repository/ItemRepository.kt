package com.mustafafaraz.locateme.data.repository

import android.content.Context
import android.util.Log
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.local.AppDatabase
import com.mustafafaraz.locateme.data.local.entity.ItemEntity
import com.mustafafaraz.locateme.data.model.Item
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository pattern for Items
 * Implements offline-first strategy with cache and sync
 */
class ItemRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val itemDao = database.itemDao()
    private val tokenManager = TokenManager(context)

    companion object {
        private const val TAG = "ItemRepository"
    }

    /**
     * Get all items - Returns cached data immediately, syncs in background if online
     */
    fun getAllItems(): Flow<List<Item>> {
        // Return cached data as Flow
        return itemDao.getAllItems().map { entities ->
            entities.map { it.toItem() }
        }
    }

    /**
     * Sync items from server and update cache
     */
    suspend fun syncItems(type: String? = null, category: String? = null, search: String? = null): Result<List<Item>> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getItems(authHeader, type, category, search)

            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()

                // Update cache
                val entities = items.map { ItemEntity.fromItem(it) }
                itemDao.insertItems(entities)

                Log.d(TAG, "✅ Synced ${items.size} items to cache")
                Result.success(items)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing items", e)
            Result.failure(e)
        }
    }

    /**
     * Get saved items from cache
     */
    fun getSavedItems(): Flow<List<Item>> {
        return itemDao.getSavedItems().map { entities ->
            entities.map { it.toItem() }
        }
    }

    /**
     * Sync saved items from server
     */
    suspend fun syncSavedItems(): Result<List<Item>> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getSavedItems(authHeader)

            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()

                // Update cache - mark items as saved
                items.forEach { item ->
                    val entity = ItemEntity.fromItem(item.copy(isSaved = true))
                    itemDao.insertItem(entity)
                }

                Log.d(TAG, "✅ Synced ${items.size} saved items")
                Result.success(items)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch saved items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing saved items", e)
            Result.failure(e)
        }
    }

    /**
     * Get my items from cache
     */
    fun getMyItems(userId: Int, type: String? = null, status: String? = null): Flow<List<Item>> {
        return when {
            type != null -> itemDao.getMyItemsByType(userId, type)
            status != null -> itemDao.getMyItemsByStatus(userId, status)
            else -> itemDao.getMyItems(userId)
        }.map { entities ->
            entities.map { it.toItem() }
        }
    }

    /**
     * Sync my items from server
     */
    suspend fun syncMyItems(type: String? = null, status: String? = null): Result<List<Item>> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection"))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"
            val response = RetrofitClient.apiService.getMyItems(authHeader, type, status)

            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()

                // Update cache
                val entities = items.map { ItemEntity.fromItem(it) }
                itemDao.insertItems(entities)

                Log.d(TAG, "✅ Synced ${items.size} my items")
                Result.success(items)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch my items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing my items", e)
            Result.failure(e)
        }
    }

    /**
     * Save/unsave item
     */
    suspend fun toggleSaveItem(itemId: Int, currentlySaved: Boolean): Result<Unit> {
        return try {
            if (!NetworkUtils.isOnline(context)) {
                return Result.failure(Exception("No internet connection. Please try again when online."))
            }

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Not authenticated"))
            }

            val authHeader = "Bearer $token"

            val response = if (currentlySaved) {
                // Unsave
                RetrofitClient.apiService.unsaveItem(authHeader, itemId)
            } else {
                // Save
                RetrofitClient.apiService.saveItem(authHeader, com.mustafafaraz.locateme.data.model.SaveItemRequest(itemId))
            }

            if (response.isSuccessful) {
                // Update local cache
                itemDao.updateSavedStatus(itemId, !currentlySaved)
                Log.d(TAG, "✅ Item ${if (currentlySaved) "unsaved" else "saved"}")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update item"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save item", e)
            Result.failure(e)
        }
    }

    /**
     * Clear all cached items (on logout)
     */
    suspend fun clearCache() {
        itemDao.deleteAllItems()
        Log.d(TAG, "✅ Cache cleared")
    }

    /**
     * Clean old cache (delete items older than 7 days)
     */
    suspend fun cleanOldCache() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        itemDao.deleteOldItems(sevenDaysAgo)
        Log.d(TAG, "✅ Old cache cleaned")
    }
}

