package com.mustafafaraz.locateme.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mustafafaraz.locateme.data.local.dao.ItemDao
import com.mustafafaraz.locateme.data.local.dao.MessageDao
import com.mustafafaraz.locateme.data.local.dao.SyncQueueDao
import com.mustafafaraz.locateme.data.local.dao.UserProfileDao
import com.mustafafaraz.locateme.data.local.entity.ItemEntity
import com.mustafafaraz.locateme.data.local.entity.MessageEntity
import com.mustafafaraz.locateme.data.local.entity.SyncQueueEntity
import com.mustafafaraz.locateme.data.local.entity.UserProfileEntity

@Database(
    entities = [
        ItemEntity::class,
        UserProfileEntity::class,
        MessageEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun messageDao(): MessageDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "locateme_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
