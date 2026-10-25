package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.XpEventDao
import com.vrddhih.app.core.data.local.entity.XpEventEntity
import com.vrddhih.app.core.data.local.entity.XpSource
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class ProgressionRepository(
    private val dao: XpEventDao,
    private val clock: Clock,
) {
    fun observeTotalXp(): Flow<Long> = dao.observeTotalXp()

    fun observeEventsOn(date: LocalDate): Flow<List<XpEventEntity>> = dao.observeEventsOn(date)

    /** Appends an XP event. Returns false if [sourceKey] already awarded XP for [source]. */
    suspend fun recordXp(source: XpSource, sourceKey: String, amount: Int, date: LocalDate): Boolean =
        dao.insertEvent(
            XpEventEntity(
                source = source,
                sourceKey = sourceKey,
                amount = amount,
                date = date,
                occurredAt = Instant.now(clock),
            ),
        ) != -1L
}
