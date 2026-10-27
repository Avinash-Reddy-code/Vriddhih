package com.vrddhih.app.core.data.quest

import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class QuestRecurrenceTest {

    private val now = Instant.parse("2026-10-25T10:00:00Z")

    private fun quest(
        recurrence: QuestRecurrence,
        startDate: LocalDate,
        interval: Int = 1,
        daysMask: Int = 0,
        endDate: LocalDate? = null,
        isArchived: Boolean = false,
    ) = QuestEntity(
        id = 1,
        title = "Test Quest",
        startDate = startDate,
        recurrence = recurrence,
        recurrenceInterval = interval,
        recurrenceDaysMask = daysMask,
        endDate = endDate,
        isArchived = isArchived,
        createdAt = now,
        updatedAt = now,
    )

    @Test
    fun dailyRecurrence_everyDay() {
        val q = quest(QuestRecurrence.DAILY, startDate = LocalDate.of(2026, 10, 20))
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 19)))
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 20)))
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 21)))
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 31)))
        assertTrue(q.occursOn(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun dailyRecurrence_every3Days() {
        val q = quest(QuestRecurrence.DAILY, startDate = LocalDate.of(2026, 10, 20), interval = 3)
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 20)))
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 21)))
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 22)))
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 23)))
    }

    @Test
    fun weekdaysRecurrence_occursOnlyOnSelectedDays() {
        val mask = QuestEntity.daysMaskOf(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        // 2026-10-19 is a Monday
        val q = quest(QuestRecurrence.WEEKDAYS, startDate = LocalDate.of(2026, 10, 19), daysMask = mask)

        assertTrue(q.occursOn(LocalDate.of(2026, 10, 19))) // Mon
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 20))) // Tue
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 21))) // Wed
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 22))) // Thu
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 23))) // Fri
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 24))) // Sat
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 25))) // Sun
    }

    @Test
    fun weeklyRecurrence_occursSameWeekdayEveryNWeeks() {
        // 2026-10-25 is a Sunday
        val q = quest(QuestRecurrence.WEEKLY, startDate = LocalDate.of(2026, 10, 25), interval = 2)

        assertTrue(q.occursOn(LocalDate.of(2026, 10, 25)))
        assertFalse(q.occursOn(LocalDate.of(2026, 11, 1))) // Week 1: false
        assertTrue(q.occursOn(LocalDate.of(2026, 11, 8))) // Week 2: true
    }

    @Test
    fun monthlyRecurrence_handlesMonthLengthsAndClamping() {
        // 31st of January: February should clamp to Feb 28 (or 29 on leap year)
        val q = quest(QuestRecurrence.MONTHLY, startDate = LocalDate.of(2026, 1, 31))

        assertTrue(q.occursOn(LocalDate.of(2026, 1, 31)))
        assertTrue(q.occursOn(LocalDate.of(2026, 2, 28))) // 2026 is non-leap
        assertTrue(q.occursOn(LocalDate.of(2026, 3, 31)))
        assertTrue(q.occursOn(LocalDate.of(2026, 4, 30))) // April has 30 days
        assertFalse(q.occursOn(LocalDate.of(2026, 4, 29)))
    }

    @Test
    fun monthlyRecurrence_leapYearFebruary29() {
        // 2024 was a leap year
        val q = quest(QuestRecurrence.MONTHLY, startDate = LocalDate.of(2024, 1, 31))
        assertTrue(q.occursOn(LocalDate.of(2024, 2, 29)))
        assertFalse(q.occursOn(LocalDate.of(2024, 2, 28)))
    }

    @Test
    fun recurrence_respectsEndDateAndArchive() {
        val q = quest(
            QuestRecurrence.DAILY,
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 5),
        )
        assertTrue(q.occursOn(LocalDate.of(2026, 10, 5)))
        assertFalse(q.occursOn(LocalDate.of(2026, 10, 6)))

        val archived = q.copy(isArchived = true)
        assertFalse(archived.occursOn(LocalDate.of(2026, 10, 3)))
    }

    @Test
    fun occurrencesBetween_isBoundedAndIdempotent() {
        val q = quest(QuestRecurrence.DAILY, startDate = LocalDate.of(2026, 10, 1), interval = 2)
        val occurrences = q.occurrencesBetween(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7))
        assertEquals(
            listOf(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 7),
            ),
            occurrences,
        )
    }

    @Test
    fun nextOccurrenceOnOrAfter_findsFirstMatchingDate() {
        val q = quest(QuestRecurrence.WEEKLY, startDate = LocalDate.of(2026, 10, 18)) // Sunday
        assertEquals(
            LocalDate.of(2026, 10, 25),
            q.nextOccurrenceOnOrAfter(LocalDate.of(2026, 10, 20)),
        )
    }
}
