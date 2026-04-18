package com.example.frienddb.data.db.entities

data class DayUnavailability(
    val id: Long,
    val friendId: Long,
    val startDate: Long,
    val endDate: Long,
    val reason: String,
    val friendName: String,
    val friendColor: Int
)
