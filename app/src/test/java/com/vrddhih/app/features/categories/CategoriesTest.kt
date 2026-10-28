package com.vrddhih.app.features.categories

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vrddhih.app.core.data.category.BuiltInCategories
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.repository.CategoryRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class CategoriesTest {

    private lateinit var db: VrddhihDatabase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var questRepository: QuestRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var goalRepository: GoalRepository
    private val clock = Clock.fixed(Instant.parse("2026-10-28T00:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VrddhihDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = CategoryRepository(db)
        questRepository = QuestRepository(db, clock)
        habitRepository = HabitRepository(db, clock)
        goalRepository = GoalRepository(db, clock)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun defaultCategories_hasExactlySixExpectedCategories() = runTest {
        categoryRepository.seedBuiltIns()

        val all = categoryRepository.getAll()
        val builtIn = all.filter { it.isBuiltIn }
        val names = builtIn.map { it.name }.toSet()

        assertEquals(6, builtIn.size)
        assertEquals(
            setOf("Body", "Mind", "Work", "Fitness", "Sleep", "Health"),
            names,
        )
    }

    @Test
    fun seedBuiltIns_isIdempotent() = runTest {
        categoryRepository.seedBuiltIns()
        categoryRepository.seedBuiltIns()

        val all = categoryRepository.getAll()
        val builtIn = all.filter { it.isBuiltIn }

        assertEquals(6, builtIn.size)
    }

    @Test
    fun seedBuiltIns_preservesObsoleteCategoriesWithExistingQuestsAsCustom() = runTest {
        // Manually insert an obsolete built-in category
        db.categoryDao().insert(
            CategoryEntity(
                id = "builtin_coding",
                name = "Coding",
                iconKey = "code",
                colorArgb = 0xFF00E5FF.toInt(),
                isBuiltIn = true,
                sortOrder = 99,
            ),
        )

        // Attach a quest to this category
        val today = LocalDate.of(2026, 10, 28)
        questRepository.createQuest(
            QuestDraft(
                title = "Antigravity Feature",
                scheduledDate = today,
                categoryId = "builtin_coding",
            ),
        )

        // Seed built-ins
        categoryRepository.seedBuiltIns()

        val all = categoryRepository.getAll()
        val coding = all.find { it.id == "builtin_coding" }

        // Must still exist, but now as a custom category
        assertNotNull(coding)
        assertFalse(coding!!.isBuiltIn)
        assertEquals("Coding", coding.name)

        // Default categories must still be exactly 6
        val builtIn = all.filter { it.isBuiltIn }
        assertEquals(6, builtIn.size)
    }

    @Test
    fun seedBuiltIns_removesUnreferencedObsoleteCategories() = runTest {
        // Manually insert an obsolete category with no references
        db.categoryDao().insert(
            CategoryEntity(
                id = "builtin_errands",
                name = "Errands",
                iconKey = "shopping_cart",
                colorArgb = 0xFFCE93D8.toInt(),
                isBuiltIn = true,
                sortOrder = 99,
            ),
        )

        categoryRepository.seedBuiltIns()

        val all = categoryRepository.getAll()
        val errands = all.find { it.id == "builtin_errands" }
        assertEquals(null, errands)
    }

    @Test
    fun observeEntryCounts_correctlyCalculatesAcrossQuestsHabitsAndGoals() = runTest {
        categoryRepository.seedBuiltIns()

        val today = LocalDate.of(2026, 10, 28)
        // Add 1 quest in Fitness
        questRepository.createQuest(
            QuestDraft(title = "Pushups", scheduledDate = today, categoryId = "builtin_fitness"),
        )
        // Add 1 habit in Fitness
        db.habitDao().insertHabit(
            HabitEntity(
                name = "Morning Jog",
                startDate = today,
                categoryId = "builtin_fitness",
                createdAt = clock.instant(),
                updatedAt = clock.instant(),
            ),
        )
        // Add 1 goal in Fitness
        db.goalDao().insertGoal(
            GoalEntity(
                title = "Run 10km",
                categoryId = "builtin_fitness",
                createdAt = clock.instant(),
                updatedAt = clock.instant(),
            ),
        )

        val counts = categoryRepository.observeEntryCounts().first()
        assertEquals(3, counts["builtin_fitness"])
        assertEquals(0, counts["builtin_mind"] ?: 0)
    }

    @Test
    fun customCategory_createAndEntryTracking() = runTest {
        categoryRepository.seedBuiltIns()

        val custom = categoryRepository.createCustom("Reading", "book", 0xFF7986CB.toInt())
        assertNotNull(custom)

        val today = LocalDate.of(2026, 10, 28)
        questRepository.createQuest(
            QuestDraft(title = "Read 20 pages", scheduledDate = today, categoryId = custom!!.id),
        )

        val counts = categoryRepository.observeEntryCounts().first()
        assertEquals(1, counts[custom.id])
    }
}
