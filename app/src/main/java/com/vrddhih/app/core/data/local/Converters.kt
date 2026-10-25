package com.vrddhih.app.core.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

/**
 * Date/time storage semantics:
 * - [LocalDate] is a calendar day with no time zone, stored as epoch day (days since 1970-01-01).
 *   It never passes through UTC timestamps, so a stored day can't shift when the device's time zone changes.
 * - [Instant] is an exact moment, stored as epoch milliseconds (UTC).
 */
class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMilliToInstant(epochMilli: Long?): Instant? = epochMilli?.let(Instant::ofEpochMilli)
}
