package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    /** Days the habit is scheduled, as a bitmask over ISO weekdays (bit 0 = Monday … bit 6 = Sunday). */
    @ColumnInfo(name = "schedule_days_mask", defaultValue = "127") val scheduleDaysMask: Int = EVERY_DAY_MASK,
    @ColumnInfo(name = "xp_per_check_in", defaultValue = "5") val xpPerCheckIn: Int = 5,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
) {
    fun isScheduledOn(date: LocalDate): Boolean =
        !date.isBefore(startDate) && scheduleDaysMask and date.dayOfWeek.maskBit() != 0

    companion object {
        const val EVERY_DAY_MASK = 0b111_1111

        fun maskOf(vararg days: DayOfWeek): Int = days.fold(0) { mask, day -> mask or day.maskBit() }

        private fun DayOfWeek.maskBit(): Int = 1 shl (value - 1)
    }
}

/** A habit check-in for a calendar day. Unique per (habit_id, date), so repeated check-ins can't duplicate. */
@Entity(
    tableName = "habit_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true), Index("date")],
)
data class HabitCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "checked_in_at") val checkedInAt: Instant,
)
