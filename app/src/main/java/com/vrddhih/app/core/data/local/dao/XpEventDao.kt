package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vrddhih.app.core.data.local.entity.XpEventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface XpEventDao {
    /** Returns -1 when an event with the same (source, source_key) already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: XpEventEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_events")
    fun observeTotalXp(): Flow<Long>

    @Query("SELECT * FROM xp_events WHERE date = :date ORDER BY occurred_at, id")
    fun observeEventsOn(date: LocalDate): Flow<List<XpEventEntity>>
}
