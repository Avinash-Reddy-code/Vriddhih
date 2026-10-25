package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class XpSource { QUEST_COMPLETION, HABIT_CHECK_IN, MILESTONE_COMPLETED, GOAL_COMPLETED, ADJUSTMENT }

/**
 * Append-only progression ledger. Total XP and level are derived from it, never stored separately.
 *
 * [sourceKey] identifies what produced the event (for example `quest:12:2026-10-25`). The unique
 * (source, source_key) index stops the same action from awarding XP twice. There is deliberately no
 * foreign key, so earned XP history survives if the source record is later deleted.
 */
@Entity(
    tableName = "xp_events",
    indices = [Index(value = ["source", "source_key"], unique = true), Index("date")],
)
data class XpEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: XpSource,
    @ColumnInfo(name = "source_key") val sourceKey: String,
    /** XP delta. Negative values are allowed for reversals and adjustments. */
    val amount: Int,
    /** The progression day the XP counts towards. */
    val date: LocalDate,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
)
