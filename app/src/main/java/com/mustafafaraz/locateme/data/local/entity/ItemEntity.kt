package com.mustafafaraz.locateme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mustafafaraz.locateme.data.model.Item

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String, // LOST or FOUND
    val status: String, // ACTIVE, RESOLVED, EXPIRED
    val imageUrls: String?, // JSON array as string
    val userName: String?,
    val userEmail: String?,
    val userProfilePic: String?,
    val dateReported: String,
    val expiresAt: String?,
    val isSaved: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    // Convert to API model
    fun toItem(): Item {
        return Item(
            id = id,
            userId = userId,
            title = title,
            description = description,
            _imageUrls = if (imageUrls.isNullOrEmpty()) emptyList() else {
                // Parse JSON array string to list
                try {
                    imageUrls.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotEmpty() }
                } catch (_: Exception) {
                    emptyList()
                }
            },
            category = category,
            location = location,
            type = type,
            status = status,
            userName = userName ?: "",
            userEmail = userEmail ?: "",
            userStudentId = null,
            userBatch = null,
            userDepartment = null,
            userSection = null,
            _userProfilePic = userProfilePic,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSaved = isSaved
        )
    }

    companion object {
        // Convert from API model
        fun fromItem(item: Item): ItemEntity {
            return ItemEntity(
                id = item.id,
                userId = item.userId,
                title = item.title,
                description = item.description,
                category = item.category,
                location = item.location,
                type = item.type,
                status = item.status,
                imageUrls = item.imageUrls.joinToString(",") { "\"$it\"" }
                    .let { "[$it]" },
                userName = item.userName,
                userEmail = item.userEmail,
                userProfilePic = item.userProfilePic,
                dateReported = item.createdAt,
                expiresAt = null,
                isSaved = item.isSaved,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
