package com.example.frienddb.data.repository

import androidx.room.withTransaction
import com.example.frienddb.data.db.FriendDbDatabase
import com.example.frienddb.data.db.entities.DayUnavailability
import com.example.frienddb.data.db.entities.FriendEntity
import com.example.frienddb.data.db.entities.UnavailabilityEntity
import com.example.frienddb.data.db.entities.UnavailabilityWithFriend
import com.example.frienddb.util.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

class FriendRepository(private val db: FriendDbDatabase) {

    val allUnavailabilitiesWithFriend: Flow<List<UnavailabilityWithFriend>> =
        db.unavailabilityDao().observeAllWithFriend()

    suspend fun getMonthUnavailabilities(month: YearMonth): List<DayUnavailability> =
        withContext(Dispatchers.IO) {
            val start = month.atDay(1).toEpochDay()
            val end   = month.atEndOfMonth().toEpochDay()
            db.unavailabilityDao().getOverlappingMonth(start, end)
        }

    suspend fun addUnavailability(
        name: String,
        startDate: LocalDate,
        endDate: LocalDate,
        reason: String
    ): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val existing = db.friendDao().findByName(name)
            val friendId = existing?.id
                ?: db.friendDao().insert(FriendEntity(name = name, color = ColorUtils.randomArgb()))
            db.unavailabilityDao().insert(
                UnavailabilityEntity(
                    friendId  = friendId,
                    startDate = startDate.toEpochDay(),
                    endDate   = endDate.toEpochDay(),
                    reason    = reason
                )
            )
        }
    }

    suspend fun deleteUnavailability(entity: UnavailabilityEntity) =
        withContext(Dispatchers.IO) {
            db.withTransaction {
                db.unavailabilityDao().delete(entity)
                val remaining = db.friendDao().countUnavailabilities(entity.friendId)
                if (remaining == 0) {
                    db.friendDao().findById(entity.friendId)?.let { db.friendDao().delete(it) }
                }
            }
        }
}
