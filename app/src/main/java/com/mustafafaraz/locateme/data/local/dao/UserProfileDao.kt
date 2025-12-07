package com.mustafafaraz.locateme.data.local.dao

import androidx.room.*
import com.mustafafaraz.locateme.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    // Get current user profile
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    // Get user profile as single value (one-time fetch)
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    // Insert or update user profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Update user profile
    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    // Delete user profile (on logout)
    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()
}

