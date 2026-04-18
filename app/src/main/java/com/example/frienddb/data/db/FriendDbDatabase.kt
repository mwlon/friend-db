package com.example.frienddb.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.frienddb.data.db.entities.FriendEntity
import com.example.frienddb.data.db.entities.UnavailabilityEntity

@Database(
    entities = [FriendEntity::class, UnavailabilityEntity::class],
    version  = 1,
    exportSchema = true
)
abstract class FriendDbDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun unavailabilityDao(): UnavailabilityDao

    companion object {
        @Volatile private var INSTANCE: FriendDbDatabase? = null

        fun getInstance(context: Context): FriendDbDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FriendDbDatabase::class.java,
                    "friend_db"
                ).build().also { INSTANCE = it }
            }
    }
}
