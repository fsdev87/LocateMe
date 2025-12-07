package com.mustafafaraz.locateme.data.local.dao

import androidx.room.*
import com.mustafafaraz.locateme.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    // Get all items (for home feed)
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    // Get all items as list (one-time fetch)
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    suspend fun getAllItemsList(): List<ItemEntity>

    // Get items by type
    @Query("SELECT * FROM items WHERE type = :type ORDER BY createdAt DESC")
    fun getItemsByType(type: String): Flow<List<ItemEntity>>

    // Get items by category
    @Query("SELECT * FROM items WHERE category = :category ORDER BY createdAt DESC")
    fun getItemsByCategory(category: String): Flow<List<ItemEntity>>

    // Get items by type and category
    @Query("SELECT * FROM items WHERE type = :type AND category = :category ORDER BY createdAt DESC")
    fun getItemsByTypeAndCategory(type: String, category: String): Flow<List<ItemEntity>>

    // Search items
    @Query("""
        SELECT * FROM items 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR location LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchItems(query: String): Flow<List<ItemEntity>>

    // Get item by ID
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): ItemEntity?

    // Get saved items
    @Query("SELECT * FROM items WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedItems(): Flow<List<ItemEntity>>

    // Get my items (by user ID)
    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMyItems(userId: Int): Flow<List<ItemEntity>>

    // Get my items by type
    @Query("SELECT * FROM items WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun getMyItemsByType(userId: Int, type: String): Flow<List<ItemEntity>>

    // Get my items by status
    @Query("SELECT * FROM items WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    fun getMyItemsByStatus(userId: Int, status: String): Flow<List<ItemEntity>>

    // Insert single item
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    // Insert multiple items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    // Update item
    @Update
    suspend fun updateItem(item: ItemEntity)

    // Update saved status
    @Query("UPDATE items SET isSaved = :isSaved WHERE id = :itemId")
    suspend fun updateSavedStatus(itemId: Int, isSaved: Boolean)

    // Delete item by ID
    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Int)

    // Delete all items
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    // Delete old items (cache cleanup - older than 7 days)
    @Query("DELETE FROM items WHERE lastSyncedAt < :timestamp")
    suspend fun deleteOldItems(timestamp: Long)
}

