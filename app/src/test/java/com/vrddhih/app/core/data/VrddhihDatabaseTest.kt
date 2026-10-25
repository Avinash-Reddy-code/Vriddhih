package com.vrddhih.app.core.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.dao.FinanceTotals
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestRecurrence
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.local.entity.TransactionType
import com.vrddhih.app.core.data.local.entity.XpSource
import com.vrddhih.app.core.data.repository.DayActivity
import com.vrddhih.app.core.data.repository.DayActivityRepository
import com.vrddhih.app.core.data.repository.FinanceRepository
import com.vrddhih.app.core.data.repository.GoalRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.core.data.repository.ProgressionRepository
import com.vrddhih.app.core.data.repository.QuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class VrddhihDatabaseTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-25T04:30:00Z"), ZoneOffset.UTC)
    private val day = LocalDate.of(2026, 10, 25)

    private lateinit var db: VrddhihDatabase
    private lateinit var quests: QuestRepository
    private lateinit var habits: HabitRepository
    private lateinit var goals: GoalRepository
    private lateinit var finance: FinanceRepository
    private lateinit var progression: ProgressionRepository
    private lateinit var dayActivity: DayActivityRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        quests = QuestRepository(db.questDao(), clock)
        habits = HabitRepository(db.habitDao(), clock)
        goals = GoalRepository(db.goalDao(), clock)
        finance = FinanceRepository(db.financeDao(), clock)
        progression = ProgressionRepository(db.xpEventDao(), clock)
        dayActivity = DayActivityRepository(db.questDao(), db.habitDao(), db.financeDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun database_opensEmptyWithForeignKeysEnforced() = runTest {
        db.openHelper.writableDatabase.query("PRAGMA foreign_keys").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        assertTrue(quests.observeActiveQuests().first().isEmpty())
        assertEquals(DayActivity(0, 0, 0), dayActivity.observeDayActivity(day).first())
        assertEquals(0L, progression.observeTotalXp().first())
    }

    @Test
    fun quest_insertReadUpdateDelete() = runTest {
        val id = quests.addQuest(title = "  Read 20 pages ", startDate = day, priority = TaskPriority.HIGH, xpReward = 30)
        val stored = checkNotNull(quests.getQuest(id))
        assertEquals("Read 20 pages", stored.title)
        assertEquals(TaskPriority.HIGH, stored.priority)
        assertEquals(day, stored.startDate)
        assertEquals(Instant.now(clock), stored.createdAt)

        quests.updateQuest(stored.copy(title = "Read 30 pages"))
        assertEquals("Read 30 pages", quests.getQuest(id)?.title)

        quests.deleteQuest(checkNotNull(quests.getQuest(id)))
        assertNull(quests.getQuest(id))
    }

    @Test
    fun observeQuestsOn_appliesRecurrenceRules() = runTest {
        val oneOff = quests.addQuest(title = "One-off", startDate = day)
        val daily = quests.addQuest(title = "Daily", startDate = day.minusDays(10), recurrence = QuestRecurrence.DAILY)
        val weekly = quests.addQuest(title = "Weekly", startDate = day.minusWeeks(2), recurrence = QuestRecurrence.WEEKLY)
        quests.addQuest(title = "Ended", startDate = day.minusDays(10), recurrence = QuestRecurrence.DAILY, endDate = day.minusDays(1))
        val future = quests.addQuest(title = "Future", startDate = day.plusDays(1), recurrence = QuestRecurrence.DAILY)
        val archived = quests.addQuest(title = "Archived", startDate = day, recurrence = QuestRecurrence.DAILY)
        quests.archiveQuest(archived)

        assertEquals(setOf(oneOff, daily, weekly), quests.observeQuestsOn(day).first().map { it.id }.toSet())
        assertEquals(setOf(daily, future), quests.observeQuestsOn(day.plusDays(1)).first().map { it.id }.toSet())
        assertEquals(setOf(daily, weekly, future), quests.observeQuestsOn(day.plusWeeks(1)).first().map { it.id }.toSet())
    }

    @Test
    fun questCompletion_isUniquePerOccurrence() = runTest {
        val id = quests.addQuest(title = "Daily", startDate = day, recurrence = QuestRecurrence.DAILY)

        assertTrue(quests.completeOccurrence(id, day, xpAwarded = 10))
        assertFalse(quests.completeOccurrence(id, day, xpAwarded = 10))
        assertTrue(quests.completeOccurrence(id, day.plusDays(1), xpAwarded = 10))

        assertEquals(1, quests.observeCompletionsOn(day).first().size)
        assertTrue(quests.undoCompletion(id, day))
        assertFalse(quests.undoCompletion(id, day))
        assertTrue(quests.observeCompletionsOn(day).first().isEmpty())
    }

    @Test
    fun questCompletion_requiresAnExistingQuest() = runTest {
        try {
            db.questDao().insertCompletion(
                QuestCompletionEntity(questId = 999, occurrenceDate = day, completedAt = Instant.now(clock)),
            )
            fail("Expected a foreign-key violation")
        } catch (expected: SQLiteConstraintException) {
            // Foreign keys are enforced even with OnConflictStrategy.IGNORE.
        }
    }

    @Test
    fun deletingQuest_cascadesToItsCompletions() = runTest {
        val id = quests.addQuest(title = "Daily", startDate = day, recurrence = QuestRecurrence.DAILY)
        quests.completeOccurrence(id, day, xpAwarded = 10)

        quests.deleteQuest(checkNotNull(quests.getQuest(id)))

        assertTrue(quests.observeCompletionsOn(day).first().isEmpty())
    }

    @Test
    fun deletingGoal_cascadesMilestonesAndUnlinksQuests() = runTest {
        val goalId = goals.addGoal(title = "Run a 10K", category = "Health", targetDate = day.plusMonths(3))
        val milestoneId = goals.addMilestone(goalId, "Run 5K", position = 0)
        goals.addMilestone(goalId, "Run 8K", position = 1)
        val questId = quests.addQuest(title = "Morning run", startDate = day, goalId = goalId)

        assertTrue(goals.setMilestoneCompleted(milestoneId, completed = true))
        val withMilestones = checkNotNull(goals.observeGoalWithMilestones(goalId).first())
        assertEquals(2, withMilestones.milestones.size)
        assertNotNull(withMilestones.milestones.first { it.id == milestoneId }.completedAt)

        goals.deleteGoal(withMilestones.goal)

        assertNull(goals.observeGoalWithMilestones(goalId).first())
        assertTrue(db.goalDao().getMilestones(goalId).isEmpty())
        assertNull(quests.getQuest(questId)?.goalId)
        assertNotNull(quests.getQuest(questId))
    }

    @Test
    fun habitCheckIn_isUniquePerDayAndRespectsSchedule() = runTest {
        val weekdaysOnly = HabitEntity.maskOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        )
        val meditate = habits.addHabit(name = "Meditate", startDate = day.minusDays(7))
        habits.addHabit(name = "Gym", startDate = day.minusDays(7), scheduleDaysMask = weekdaysOnly)

        // 2026-10-25 is a Sunday.
        assertEquals(listOf("Meditate"), habits.observeHabitsScheduledOn(day).first().map { it.name })
        assertEquals(2, habits.observeHabitsScheduledOn(day.plusDays(1)).first().size)

        assertTrue(habits.checkIn(meditate, day))
        assertFalse(habits.checkIn(meditate, day))
        assertEquals(1, habits.observeCheckInsOn(day).first().size)
        assertTrue(habits.undoCheckIn(meditate, day))
        assertTrue(habits.observeCheckInsOn(day).first().isEmpty())
    }

    @Test
    fun financeTotals_useIntegerMinorUnitsWithinTheMonth() = runTest {
        finance.addTransaction(TransactionType.INCOME, amountMinor = 50_000_00, date = LocalDate.of(2026, 10, 1), category = "Salary")
        finance.addTransaction(TransactionType.EXPENSE, amountMinor = 12_50, date = day, category = "Tea")
        finance.addTransaction(TransactionType.EXPENSE, amountMinor = 1_000_00, date = LocalDate.of(2026, 10, 31))
        finance.addTransaction(TransactionType.EXPENSE, amountMinor = 99_99, date = LocalDate.of(2026, 11, 1))

        val october = finance.observeMonthTotals(YearMonth.of(2026, 10)).first()
        assertEquals(FinanceTotals(incomeMinor = 50_000_00, expenseMinor = 1_012_50), october)
        assertEquals(48_987_50L, october.netMinor)
        assertEquals(FinanceTotals(0, 0), finance.observeMonthTotals(YearMonth.of(2026, 9)).first())
        assertEquals(1, finance.observeTransactionsOn(day).first().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun financeTransaction_rejectsNonPositiveAmounts() = runTest {
        finance.addTransaction(TransactionType.EXPENSE, amountMinor = 0, date = day)
    }

    @Test
    fun xpEvents_areDeduplicatedBySourceKey() = runTest {
        assertTrue(progression.recordXp(XpSource.QUEST_COMPLETION, "quest:1:$day", amount = 30, date = day))
        assertFalse(progression.recordXp(XpSource.QUEST_COMPLETION, "quest:1:$day", amount = 30, date = day))
        assertTrue(progression.recordXp(XpSource.HABIT_CHECK_IN, "quest:1:$day", amount = 5, date = day))

        assertEquals(35L, progression.observeTotalXp().first())
        assertEquals(2, progression.observeEventsOn(day).first().size)
    }

    @Test
    fun dayActivity_countsOnlyRecordsOnTheRequestedDay() = runTest {
        val questId = quests.addQuest(title = "Daily", startDate = day.minusDays(1), recurrence = QuestRecurrence.DAILY)
        val habitId = habits.addHabit(name = "Stretch", startDate = day.minusDays(1))
        quests.completeOccurrence(questId, day, xpAwarded = 10)
        quests.completeOccurrence(questId, day.minusDays(1), xpAwarded = 10)
        habits.checkIn(habitId, day)
        finance.addTransaction(TransactionType.EXPENSE, amountMinor = 250_00, date = day)

        assertEquals(DayActivity(questCompletions = 1, habitCheckIns = 1, transactions = 1), dayActivity.observeDayActivity(day).first())
        assertEquals(DayActivity(questCompletions = 1, habitCheckIns = 0, transactions = 0), dayActivity.observeDayActivity(day.minusDays(1)).first())
        assertTrue(dayActivity.observeDayActivity(day.plusDays(1)).first().isEmpty)
    }
}
