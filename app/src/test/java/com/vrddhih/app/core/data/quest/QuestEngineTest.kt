package com.vrddhih.app.core.data.quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.ActivityType
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.repository.QuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class QuestEngineTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-25T08:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 25)

    private lateinit var db: VrddhihDatabase
    private lateinit var repository: QuestRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VrddhihDatabase.SEED_CALLBACK)
            .build()
        repository = QuestRepository(db, clock)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun questDraft_validationRejectsInvalidInputs() {
        val emptyTitle = QuestDraft(title = "   ", scheduledDate = today)
        assertTrue(QuestDraftError.TITLE_REQUIRED in emptyTitle.validate())

        val longTitle = QuestDraft(title = "A".repeat(121), scheduledDate = today)
        assertTrue(QuestDraftError.TITLE_TOO_LONG in longTitle.validate())

        val invalidDeadline = QuestDraft(
            title = "Task",
            scheduledDate = today,
            deadline = today.minusDays(1),
        )
        assertTrue(QuestDraftError.DEADLINE_BEFORE_DATE in invalidDeadline.validate())

        val invalidEnd = QuestDraft(
            title = "Task",
            scheduledDate = today,
            recurrence = QuestRecurrence.DAILY,
            endDate = today.minusDays(1),
        )
        assertTrue(QuestDraftError.END_BEFORE_START in invalidEnd.validate())

        val noWeekdays = QuestDraft(
            title = "Task",
            scheduledDate = today,
            recurrence = QuestRecurrence.WEEKDAYS,
            weekdays = emptySet(),
        )
        assertTrue(QuestDraftError.NO_WEEKDAYS in noWeekdays.validate())

        val invalidInterval = QuestDraft(
            title = "Task",
            scheduledDate = today,
            recurrence = QuestRecurrence.DAILY,
            intervalText = "0",
        )
        assertTrue(QuestDraftError.INTERVAL_OUT_OF_RANGE in invalidInterval.validate())
    }

    @Test
    fun repeatInterval_validatesIntervalRange() {
        val validIntervals = listOf("1", "2", "9", "10", "999")
        for (interval in validIntervals) {
            val draft = QuestDraft(
                title = "Interval Test",
                scheduledDate = today,
                recurrence = QuestRecurrence.DAILY,
                intervalText = interval,
            )
            assertTrue("Expected $interval to be valid", QuestDraftError.INTERVAL_OUT_OF_RANGE !in draft.validate())
            assertEquals(interval.toInt(), draft.parsedInterval)
        }

        val invalidIntervals = listOf("0", "-1", "1000", "", "abc")
        for (interval in invalidIntervals) {
            val draft = QuestDraft(
                title = "Invalid Interval Test",
                scheduledDate = today,
                recurrence = QuestRecurrence.DAILY,
                intervalText = interval,
            )
            assertTrue("Expected $interval to be invalid", QuestDraftError.INTERVAL_OUT_OF_RANGE in draft.validate())
        }
    }

    @Test
    fun endDate_validAndInvalidForAllRecurrenceTypes() {
        val allRecurrences = QuestRecurrence.entries

        for (rec in allRecurrences) {
            val weekdays = if (rec == QuestRecurrence.WEEKDAYS) setOf(DayOfWeek.MONDAY) else emptySet()

            // End date before scheduled date -> invalid
            val beforeDraft = QuestDraft(
                title = "End Date Before Start",
                scheduledDate = today,
                recurrence = rec,
                weekdays = weekdays,
                endDate = today.minusDays(1),
            )
            val beforeErrors = beforeDraft.validate()
            assertTrue(
                "Expected error for recurrence $rec with end date before start",
                QuestDraftError.END_BEFORE_START in beforeErrors || QuestDraftError.DEADLINE_BEFORE_DATE in beforeErrors
            )

            // End date on start date -> valid
            val sameDayDraft = QuestDraft(
                title = "End Date Same Day",
                scheduledDate = today,
                recurrence = rec,
                weekdays = weekdays,
                endDate = today,
            )
            val sameDayErrors = sameDayDraft.validate()
            assertTrue(
                "Expected valid for recurrence $rec with end date on start date",
                QuestDraftError.END_BEFORE_START !in sameDayErrors && QuestDraftError.DEADLINE_BEFORE_DATE !in sameDayErrors
            )

            // End date after start date -> valid
            val futureDraft = QuestDraft(
                title = "End Date In Future",
                scheduledDate = today,
                recurrence = rec,
                weekdays = weekdays,
                endDate = today.plusDays(30),
            )
            val futureErrors = futureDraft.validate()
            assertTrue(
                "Expected valid for recurrence $rec with future end date",
                QuestDraftError.END_BEFORE_START !in futureErrors && QuestDraftError.DEADLINE_BEFORE_DATE !in futureErrors
            )
        }
    }

    @Test
    fun nonRecurringQuest_endDateMapsToDeadlineWithoutRecurringOccurrences() = runTest {
        val endDate = today.plusDays(10)
        val nonRecurringDraft = QuestDraft(
            title = "One-off Project",
            scheduledDate = today,
            recurrence = QuestRecurrence.NONE,
            endDate = endDate,
        )

        val id = repository.createQuest(nonRecurringDraft)
        val quest = checkNotNull(repository.getQuest(id))

        // Internally maps to deadline for non-recurring
        assertEquals(endDate, quest.deadline)
        assertNull(quest.endDate)

        // Reading back into draft populates both endDate and deadline seamlessly
        val loadedDraft = QuestDraft.from(quest)
        assertEquals(endDate, loadedDraft.endDate)
        assertEquals(endDate, loadedDraft.deadline)

        // Verify it does NOT generate recurring occurrences
        val tomorrow = today.plusDays(1)
        val tomorrowOccurrences = QuestLists.build(
            QuestBook(listOf(quest), emptyList(), emptyList()),
            QuestFilter.ALL,
            tomorrow,
        )
        assertTrue(tomorrowOccurrences.none { it.questId == id && it.date == tomorrow })
    }

    @Test
    fun switchingRecurrence_preservesEndDateAndSettings() = runTest {
        val endDate = today.plusMonths(2)
        val initialDraft = QuestDraft(
            title = "Switchable Quest",
            scheduledDate = today,
            recurrence = QuestRecurrence.NONE,
            endDate = endDate,
        )

        val id = repository.createQuest(initialDraft)
        val quest = checkNotNull(repository.getQuest(id))
        val draftFromQuest = QuestDraft.from(quest)

        // Switch to WEEKLY
        val preservedEnd = draftFromQuest.endDate ?: draftFromQuest.deadline
        val weeklyDraft = draftFromQuest.copy(
            recurrence = QuestRecurrence.WEEKLY,
            endDate = preservedEnd,
            deadline = null,
            intervalText = "2",
        )
        assertEquals(endDate, weeklyDraft.endDate)

        repository.updateQuest(id, weeklyDraft)

        val updatedQuest = checkNotNull(repository.getQuest(id))
        assertEquals(QuestRecurrence.WEEKLY, updatedQuest.recurrence)
        assertEquals(endDate, updatedQuest.endDate)
        assertEquals(2, updatedQuest.recurrenceInterval)

        // Switch back to NONE: end date must not be lost
        val backToNone = QuestDraft.from(updatedQuest).copy(
            recurrence = QuestRecurrence.NONE,
            endDate = updatedQuest.endDate,
            deadline = updatedQuest.endDate,
        )
        repository.updateQuest(id, backToNone)

        val finalQuest = checkNotNull(repository.getQuest(id))
        assertEquals(QuestRecurrence.NONE, finalQuest.recurrence)
        assertEquals(endDate, finalQuest.deadline)
    }

    @Test
    fun allTaskPriorities_persistAndRestoreCorrectly() = runTest {
        for (priority in TaskPriority.entries) {
            val draft = QuestDraft(
                title = "Priority ${priority.name}",
                scheduledDate = today,
                priority = priority,
            )
            val id = repository.createQuest(draft)
            val quest = checkNotNull(repository.getQuest(id))
            assertEquals(priority, quest.priority)
            val loadedDraft = QuestDraft.from(quest)
            assertEquals(priority, loadedDraft.priority)
        }
    }

    @Test
    fun createAndReadQuest_persistsCorrectly() = runTest {
        val draft = QuestDraft(
            title = "Daily Meditation",
            description = "10 minutes of mindfulness",
            priority = TaskPriority.HIGH,
            scheduledDate = today,
            dueTime = LocalTime.of(7, 30),
            recurrence = QuestRecurrence.DAILY,
            intervalText = "1",
            categoryId = "builtin_fitness",
        )
        val id = repository.createQuest(draft)
        val stored = checkNotNull(repository.getQuest(id))

        assertEquals("Daily Meditation", stored.title)
        assertEquals("10 minutes of mindfulness", stored.notes)
        assertEquals(TaskPriority.HIGH, stored.priority)
        assertEquals(today, stored.startDate)
        assertEquals(LocalTime.of(7, 30), stored.dueTime)
        assertEquals(QuestRecurrence.DAILY, stored.recurrence)
        assertEquals(1, stored.recurrenceInterval)
        assertEquals("builtin_fitness", stored.categoryId)
    }

    @Test
    fun completeAndReopenOccurrence_updatesStateAndLogsActivityIdempotently() = runTest {
        val id = repository.createQuest(QuestDraft(title = "Workout", scheduledDate = today))

        // Complete
        assertTrue(repository.completeOccurrence(id, today, xpAwarded = 25))
        assertFalse(repository.completeOccurrence(id, today, xpAwarded = 25)) // duplicate ignored

        var occurrences = repository.observeOccurrencesOn(today).first()
        assertEquals(1, occurrences.size)
        assertTrue(occurrences.first().isCompleted)

        var dayLog = repository.observeDayLog(today).first()
        assertEquals(1, dayLog.completedCount)
        assertEquals(1, dayLog.events.size)
        assertEquals(ActivityType.QUEST_COMPLETED, dayLog.events.first().type)

        // Reopen
        assertTrue(repository.reopenOccurrence(id, today))
        assertFalse(repository.reopenOccurrence(id, today)) // duplicate reopen ignored

        occurrences = repository.observeOccurrencesOn(today).first()
        assertFalse(occurrences.first().isCompleted)

        dayLog = repository.observeDayLog(today).first()
        assertEquals(0, dayLog.completedCount)
        assertEquals(2, dayLog.events.size)
        assertEquals(ActivityType.QUEST_COMPLETED, dayLog.events[0].type)
        assertEquals(ActivityType.QUEST_REOPENED, dayLog.events[1].type)
    }

    @Test
    fun updateOccurrence_appliesOverrideToTargetDayOnly() = runTest {
        val id = repository.createQuest(
            QuestDraft(title = "Read", scheduledDate = today, recurrence = QuestRecurrence.DAILY),
        )

        val tomorrow = today.plusDays(1)
        repository.updateOccurrence(
            questId = id,
            date = tomorrow,
            title = "Read Chapter 5",
            description = "Special chapter",
            priority = TaskPriority.CRITICAL,
            dueTime = LocalTime.of(21, 0),
        )

        val todayOcc = repository.observeOccurrencesOn(today).first().first()
        assertEquals("Read", todayOcc.title)
        assertEquals(TaskPriority.MEDIUM, todayOcc.priority)

        val tomorrowOcc = repository.observeOccurrencesOn(tomorrow).first().first()
        assertEquals("Read Chapter 5", tomorrowOcc.title)
        assertEquals("Special chapter", tomorrowOcc.notes)
        assertEquals(TaskPriority.CRITICAL, tomorrowOcc.priority)
        assertEquals(LocalTime.of(21, 0), tomorrowOcc.dueTime)
    }

    @Test
    fun updateFromDate_splitsRecurringQuestPreservingPastHistory() = runTest {
        val start = today.minusDays(3)
        val id = repository.createQuest(
            QuestDraft(title = "Jogging", scheduledDate = start, recurrence = QuestRecurrence.DAILY),
        )
        // Complete yesterday
        repository.completeOccurrence(id, today.minusDays(1), xpAwarded = 10)

        // Update from today onwards
        val splitDraft = QuestDraft(
            title = "Sprint intervals",
            scheduledDate = today,
            recurrence = QuestRecurrence.DAILY,
            priority = TaskPriority.HIGH,
        )
        val newId = repository.updateFromDate(id, today, splitDraft)

        // Original quest ended yesterday
        val original = checkNotNull(repository.getQuest(id))
        assertEquals(today.minusDays(1), original.endDate)

        // Past completion belongs to original quest
        val pastCompletions = db.questDao().getCompletionsFor(id)
        assertEquals(1, pastCompletions.size)
        assertEquals(today.minusDays(1), pastCompletions.first().occurrenceDate)

        // Today's occurrence comes from the new quest
        val todayOcc = repository.observeOccurrencesOn(today).first().first()
        assertEquals(newId, todayOcc.questId)
        assertEquals("Sprint intervals", todayOcc.title)
    }

    @Test
    fun deleteOccurrence_skipsTargetDateWithoutAffectingOtherDays() = runTest {
        val id = repository.createQuest(
            QuestDraft(title = "Daily Standup", scheduledDate = today, recurrence = QuestRecurrence.DAILY),
        )
        repository.completeOccurrence(id, today, xpAwarded = 10)

        // Delete today's occurrence
        repository.deleteOccurrence(id, today)

        // Today has no occurrences and no completions
        assertTrue(repository.observeOccurrencesOn(today).first().isEmpty())
        assertTrue(repository.observeCompletionsOn(today).first().isEmpty())

        // Tomorrow is still scheduled
        val tomorrow = repository.observeOccurrencesOn(today.plusDays(1)).first()
        assertEquals(1, tomorrow.size)
        assertEquals("Daily Standup", tomorrow.first().title)
    }

    @Test
    fun deleteFromDate_endsRecurringQuestAndPreservesPastHistory() = runTest {
        val start = today.minusDays(5)
        val id = repository.createQuest(
            QuestDraft(title = "Morning Coffee", scheduledDate = start, recurrence = QuestRecurrence.DAILY),
        )
        repository.completeOccurrence(id, today.minusDays(2), xpAwarded = 5)

        // Delete from today onwards
        repository.deleteFromDate(id, today)

        // Past completion remains
        val completions = db.questDao().getCompletionsFor(id)
        assertEquals(1, completions.size)

        // Ended before today
        val quest = checkNotNull(repository.getQuest(id))
        assertEquals(today.minusDays(1), quest.endDate)

        // No occurrences today or in future
        assertTrue(repository.observeOccurrencesOn(today).first().isEmpty())
        assertTrue(repository.observeOccurrencesOn(today.plusDays(1)).first().isEmpty())
    }

    @Test
    fun deleteQuest_deletesEntireDefinitionAndCascadeCompletionsAndActivity() = runTest {
        val id = repository.createQuest(QuestDraft(title = "One-off", scheduledDate = today))
        repository.completeOccurrence(id, today)

        repository.deleteQuest(id)

        assertNull(repository.getQuest(id))
        assertTrue(repository.observeCompletionsOn(today).first().isEmpty())
        assertTrue(repository.getActivityFor(id).isEmpty())
    }

    @Test
    fun overdueFiltering_identifiesOnlyIncompleteOneOffQuestsPastDeadline() = runTest {
        // One-off scheduled yesterday with no deadline: effective deadline is yesterday -> overdue
        val overdue1 = QuestOccurrence(
            quest = QuestEntity(
                id = 1,
                title = "Pay Bill",
                startDate = today.minusDays(1),
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock),
            ),
            date = today.minusDays(1),
        )
        assertTrue(overdue1.isOverdue(today))

        // Completed one-off scheduled yesterday: not overdue
        val completed = overdue1.copy(
            completion = com.vrddhih.app.core.data.local.entity.QuestCompletionEntity(
                questId = 1,
                occurrenceDate = today.minusDays(1),
                completedAt = Instant.now(clock),
            ),
        )
        assertFalse(completed.isOverdue(today))

        // Recurring quest scheduled in past: not overdue (recurring tasks don't accumulate as overdue)
        val recurring = QuestOccurrence(
            quest = QuestEntity(
                id = 2,
                title = "Daily Gym",
                startDate = today.minusDays(5),
                recurrence = QuestRecurrence.DAILY,
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock),
            ),
            date = today.minusDays(1),
        )
        assertFalse(recurring.isOverdue(today))

        // Future one-off: not overdue
        val future = QuestOccurrence(
            quest = QuestEntity(
                id = 3,
                title = "Doctor appointment",
                startDate = today.plusDays(2),
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock),
            ),
            date = today.plusDays(2),
        )
        assertFalse(future.isOverdue(today))
    }
}
