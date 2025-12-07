package com.mustafafaraz.locateme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mustafafaraz.locateme.data.model.User
import com.mustafafaraz.locateme.data.model.UserStats

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int,
    val fullName: String,
    val email: String,
    val studentId: String,
    val batch: String,
    val department: String,
    val section: String,
    val profilePic: String?,
    val createdAt: String,

    // Stats
    val totalItems: Int = 0,
    val resolvedItems: Int = 0,
    val successRate: Float = 0.0f,

    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toUser(): User {
        return User(
            id = id,
            fullName = fullName,
            email = email,
            studentId = studentId,
            batch = batch,
            department = department,
            section = section,
            _profilePic = profilePic,
            createdAt = createdAt,
            stats = UserStats(
                totalItems = totalItems,
                resolvedItems = resolvedItems,
                successRate = successRate
            )
        )
    }

    companion object {
        fun fromUser(user: User): UserProfileEntity {
            return UserProfileEntity(
                id = user.id,
                fullName = user.fullName,
                email = user.email,
                studentId = user.studentId,
                batch = user.batch,
                department = user.department,
                section = user.section,
                profilePic = user.profilePic,
                createdAt = user.createdAt ?: "",
                totalItems = user.stats?.totalItems ?: 0,
                resolvedItems = user.stats?.resolvedItems ?: 0,
                successRate = user.stats?.successRate ?: 0.0f
            )
        }
    }
}
