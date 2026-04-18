package com.example.frienddb.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unavailabilities",
    foreignKeys = [ForeignKey(
        entity        = FriendEntity::class,
        parentColumns = ["id"],
        childColumns  = ["friendId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("friendId")]
)
data class UnavailabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val startDate: Long,
    val endDate: Long,
    val reason: String
)
