package com.example.frienddb.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frienddb.data.db.entities.FriendEntity

@Dao
interface FriendDao {

    @Query("SELECT * FROM friends WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(friend: FriendEntity): Long

    @Delete
    suspend fun delete(friend: FriendEntity)

    @Query("SELECT COUNT(*) FROM unavailabilities WHERE friendId = :friendId")
    suspend fun countUnavailabilities(friendId: Long): Int
}
