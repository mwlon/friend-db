package com.example.frienddb.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class UnavailabilityWithFriend(
    @Embedded val unavailability: UnavailabilityEntity,
    @Relation(
        parentColumn = "friendId",
        entityColumn = "id"
    )
    val friend: FriendEntity
)
