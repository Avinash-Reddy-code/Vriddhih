package com.vrddhih.app.core.data.local

import androidx.room.TypeConverter
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import com.vrddhih.app.core.data.local.entity.HabitCompletionState
import com.vrddhih.app.core.data.local.entity.GoalType
import com.vrddhih.app.core.data.local.entity.GoalStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Date/time storage semantics:
 * - [LocalDate] is a calendar day with no time zone, stored as epoch day (days since 1970-01-01).
 *   It never passes through UTC timestamps, so a stored day can't shift when the device's time zone changes.
 * - [LocalTime] is a wall-clock time with no time zone, stored as seconds since midnight.
 * - [Instant] is an exact moment, stored as epoch milliseconds (UTC).
 */
class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localTimeToSecondOfDay(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun secondOfDayToLocalTime(secondOfDay: Int?): LocalTime? = secondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMilliToInstant(epochMilli: Long?): Instant? = epochMilli?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun habitTypeToString(type: HabitType?): String? = type?.name

    @TypeConverter
    fun stringToHabitType(value: String?): HabitType? = value?.let { HabitType.valueOf(it) }

    @TypeConverter
    fun habitScheduleTypeToString(type: HabitScheduleType?): String? = type?.name

    @TypeConverter
    fun stringToHabitScheduleType(value: String?): HabitScheduleType? = value?.let { HabitScheduleType.valueOf(it) }

    @TypeConverter
    fun numericDirectionToString(direction: NumericDirection?): String? = direction?.name

    @TypeConverter
    fun stringToNumericDirection(value: String?): NumericDirection? = value?.let { NumericDirection.valueOf(it) }

    @TypeConverter
    fun habitCompletionStateToString(state: HabitCompletionState?): String? = state?.name

    @TypeConverter
    fun stringToHabitCompletionState(value: String?): HabitCompletionState? = value?.let { HabitCompletionState.valueOf(it) }

    @TypeConverter
    fun goalTypeToString(type: GoalType?): String? = type?.name

    @TypeConverter
    fun stringToGoalType(value: String?): GoalType? = value?.let { GoalType.valueOf(it) }

    @TypeConverter
    fun goalStatusToString(status: GoalStatus?): String? = status?.name

    @TypeConverter
    fun stringToGoalStatus(value: String?): GoalStatus? = value?.let { GoalStatus.valueOf(it) }
}
