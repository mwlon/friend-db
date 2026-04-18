package com.example.frienddb.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.frienddb.data.db.entities.DayUnavailability
import com.example.frienddb.data.db.entities.UnavailabilityEntity
import com.example.frienddb.data.db.entities.UnavailabilityWithFriend
import kotlinx.coroutines.flow.Flow

@Dao
interface UnavailabilityDao {

    @Transaction
    @Query("""
        SELECT u.* FROM unavailabilities u
        JOIN friends f ON u.friendId = f.id
        ORDER BY f.name ASC, u.startDate ASC
    """)
    fun observeAllWithFriend(): Flow<List<UnavailabilityWithFriend>>

    @Query("""
        SELECT u.id, u.friendId, u.startDate, u.endDate, u.reason,
               f.name AS friendName, f.color AS friendColor
        FROM unavailabilities u
        JOIN friends f ON u.friendId = f.id
        WHERE u.startDate <= :monthEndDay AND u.endDate >= :monthStartDay
    """)
    suspend fun getOverlappingMonth(monthStartDay: Long, monthEndDay: Long): List<DayUnavailability>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UnavailabilityEntity): Long

    @Delete
    suspend fun delete(entity: UnavailabilityEntity)
}
