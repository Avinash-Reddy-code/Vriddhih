package com.vrddhih.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vrddhih.app.core.data.category.BuiltInCategories
import com.vrddhih.app.core.data.local.dao.CategoryDao
import com.vrddhih.app.core.data.local.dao.FinanceDao
import com.vrddhih.app.core.data.local.dao.GoalDao
import com.vrddhih.app.core.data.local.dao.HabitDao
import com.vrddhih.app.core.data.local.dao.QuestDao
import com.vrddhih.app.core.data.local.dao.XpEventDao
import com.vrddhih.app.core.data.local.entity.ActivityEventEntity
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.FinanceTransactionEntity
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.HabitCheckInEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.QuestOccurrenceOverrideEntity
import com.vrddhih.app.core.data.local.entity.XpEventEntity
import com.vrddhih.app.core.data.local.entity.HabitDailyProgress
import com.vrddhih.app.core.data.local.entity.HabitGoalLink
import com.vrddhih.app.core.data.local.entity.HabitNumericConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerConfig
import com.vrddhih.app.core.data.local.entity.HabitTimerSession
import com.vrddhih.app.core.data.local.entity.HabitChecklistItem
import com.vrddhih.app.core.data.local.entity.HabitChecklistProgress
import com.vrddhih.app.core.data.local.entity.HabitLimitConfig
import com.vrddhih.app.core.data.local.entity.HabitExcludedDate
import com.vrddhih.app.core.data.local.entity.GoalNumericTarget
import com.vrddhih.app.core.data.local.entity.GoalCompletionTarget

/** The app's single on-device database, stored in app-private storage. */
@Database(
    entities = [
        QuestEntity::class,
        QuestCompletionEntity::class,
        QuestOccurrenceOverrideEntity::class,
        ActivityEventEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        HabitDailyProgress::class,
        HabitNumericConfig::class,
        HabitTimerConfig::class,
        HabitTimerSession::class,
        HabitChecklistItem::class,
        HabitChecklistProgress::class,
        HabitLimitConfig::class,
        HabitExcludedDate::class,
        HabitGoalLink::class,
        GoalEntity::class,
        MilestoneEntity::class,
        GoalNumericTarget::class,
        GoalCompletionTarget::class,
        FinanceTransactionEntity::class,
        XpEventEntity::class,
        CategoryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VrddhihDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun financeDao(): FinanceDao
    abstract fun xpEventDao(): XpEventDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val NAME = "vrddhih.db"

        val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedBuiltInCategories(db)
            }
        }

        fun seedBuiltInCategories(db: SupportSQLiteDatabase) {
            // Safely remap any quests referencing legacy built-in category IDs
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_body' WHERE `category_id` = 'builtin_personal'")
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_fitness' WHERE `category_id` = 'builtin_health'")
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_mind' WHERE `category_id` = 'builtin_habits'")
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_money' WHERE `category_id` = 'builtin_finance'")
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_study' WHERE `category_id` = 'builtin_reading'")
            db.execSQL("UPDATE `quests` SET `category_id` = 'builtin_errands' WHERE `category_id` = 'builtin_appointments'")

            BuiltInCategories.ALL.forEach { cat ->
                db.execSQL(
                    "INSERT OR REPLACE INTO `categories` (`id`, `name`, `icon_key`, `color_argb`, `is_built_in`, `sort_order`) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(cat.id, cat.name, cat.iconKey, cat.colorArgb, if (cat.isBuiltIn) 1 else 0, cat.sortOrder),
                )
            }

            val currentIds = BuiltInCategories.ALL.joinToString("','") { it.id }
            db.execSQL("DELETE FROM `categories` WHERE `is_built_in` = 1 AND `id` NOT IN ('$currentIds')")
        }

        /**
         * Destructive migration fallback is intentionally not enabled. A missing migration fails loudly
         * instead of silently erasing personal data.
         */
        fun build(context: Context): VrddhihDatabase =
            Room.databaseBuilder(context.applicationContext, VrddhihDatabase::class.java, NAME)
                .addMigrations(*VrddhihMigrations.ALL)
                .addCallback(SEED_CALLBACK)
                .build()
    }
}

/**
 * Explicit schema migrations. To change the schema: bump [Database.version], add a [Migration] here,
 * commit the new exported schema in `app/schemas`, and add a migration test.
 */
object VrddhihMigrations {

    /** v2 (Phase 2): richer quest fields, per-occurrence overrides and the task activity log. Additive only. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `due_time` INTEGER")
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `deadline` INTEGER")
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `category` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `recurrence_interval` INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `recurrence_days_mask` INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `quest_occurrence_overrides` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `quest_id` INTEGER NOT NULL, " +
                    "`occurrence_date` INTEGER NOT NULL, `is_skipped` INTEGER NOT NULL DEFAULT 0, `title` TEXT, " +
                    "`notes` TEXT, `priority` TEXT, `due_time` INTEGER, `updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`quest_id`) REFERENCES `quests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_quest_occurrence_overrides_quest_id_occurrence_date` " +
                    "ON `quest_occurrence_overrides` (`quest_id`, `occurrence_date`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quest_occurrence_overrides_occurrence_date` " +
                    "ON `quest_occurrence_overrides` (`occurrence_date`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `activity_events` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `quest_id` INTEGER, " +
                    "`quest_title` TEXT NOT NULL, `occurrence_date` INTEGER NOT NULL, `occurred_at` INTEGER NOT NULL)",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_events_occurrence_date` ON `activity_events` (`occurrence_date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_events_quest_id` ON `activity_events` (`quest_id`)")
        }
    }

    /**
     * v3 (Phase 2.5 – Category Management):
     * - Adds `categories` table with built-in/custom flag.
     * - Adds `category_id` column to `quests` (TEXT, nullable FK to `categories.id`).
     * - Existing quests keep their old plain-text `category` column for historical reference; the column
     *   is NOT dropped (SQLite cannot drop columns without a full table rebuild). New saves use `category_id`.
     *
     * Data-integrity policy: existing quests get `category_id = NULL`, which the app treats as "Other"
     * at read time. Built-in categories are seeded by [CategoryRepository.seedBuiltIns] on first open.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the categories table.
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `categories` (" +
                    "`id` TEXT NOT NULL PRIMARY KEY, " +
                    "`name` TEXT NOT NULL, " +
                    "`icon_key` TEXT NOT NULL, " +
                    "`color_argb` INTEGER NOT NULL, " +
                    "`is_built_in` INTEGER NOT NULL DEFAULT 0, " +
                    "`sort_order` INTEGER NOT NULL DEFAULT 0" +
                    ")",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")

            // Add `category_id` FK column to quests (nullable, no cascade delete so quests survive category removal).
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `category_id` TEXT")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_category_id` ON `quests` (`category_id`)")

            VrddhihDatabase.seedBuiltInCategories(db)
        }
    }

    /**
     * v4 (Phase 3 - Habit Engine and Goal Tracking):
     * - Migrates habits table to support 5 habit types (YES_NO, NUMERIC, TIMER, CHECKLIST, LIMIT)
     * - Adds schedule fields: scheduleType, scheduleInterval, scheduleDaysMask, endDate
     * - Adds category, icon, color fields to habits
     * - Migrates goals table to support multiple goal types (MILESTONE_BASED, NUMERIC_TARGET, COMPLETION_BASED)
     * - Adds type, startDate, description fields to goals
     * - Creates new tables: habit_numeric_config, habit_timer_config, habit_timer_sessions,
     *   habit_checklist_items, habit_checklist_progress, habit_limit_config, habit_excluded_dates,
     *   habit_daily_progress, habit_goal_links, goal_numeric_target, goal_completion_target
     * - Migrates existing habit_check_ins to habit_daily_progress
     * - Preserves all existing habits and goals data
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Rename old habits table to habits_old
            db.execSQL("ALTER TABLE `habits` RENAME TO `habits_old`")

            // 2. Create new habits table with expanded schema
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habits` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`description` TEXT NOT NULL DEFAULT '', " +
                    "`type` TEXT NOT NULL DEFAULT 'YES_NO', " +
                    "`schedule_type` TEXT NOT NULL DEFAULT 'DAILY', " +
                    "`schedule_interval` INTEGER NOT NULL DEFAULT 1, " +
                    "`schedule_days_mask` INTEGER NOT NULL DEFAULT 127, " +
                    "`start_date` INTEGER NOT NULL, " +
                    "`end_date` INTEGER, " +
                    "`category_id` TEXT, " +
                    "`icon_key` TEXT, " +
                    "`color_argb` INTEGER, " +
                    "`is_archived` INTEGER NOT NULL DEFAULT 0, " +
                    "`xp_per_completion` INTEGER NOT NULL DEFAULT 5, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_category_id` ON `habits` (`category_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_start_date` ON `habits` (`start_date`)")

            // 3. Migrate existing habits to new table (default to YES_NO type, DAILY schedule)
            db.execSQL(
                "INSERT INTO `habits` " +
                    "(id, name, description, type, schedule_type, schedule_interval, schedule_days_mask, " +
                    "start_date, end_date, is_archived, xp_per_completion, created_at, updated_at) " +
                    "SELECT id, name, notes, 'YES_NO', 'DAILY', 1, schedule_days_mask, " +
                    "start_date, NULL, is_archived, xp_per_check_in, created_at, updated_at " +
                    "FROM `habits_old`",
            )

            // 4. Drop old habits table
            db.execSQL("DROP TABLE `habits_old`")

            // 5. Rename old goals table to goals_old
            db.execSQL("ALTER TABLE `goals` RENAME TO `goals_old`")

            // 6. Create new goals table with expanded schema
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `goals` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`description` TEXT NOT NULL DEFAULT '', " +
                    "`type` TEXT NOT NULL DEFAULT 'MILESTONE_BASED', " +
                    "`category_id` TEXT, " +
                    "`start_date` INTEGER, " +
                    "`target_date` INTEGER, " +
                    "`status` TEXT NOT NULL DEFAULT 'ACTIVE', " +
                    "`completed_at` INTEGER, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_status` ON `goals` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_category_id` ON `goals` (`category_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_target_date` ON `goals` (`target_date`)")

            // 7. Migrate existing goals to new table (default to MILESTONE_BASED type, ACTIVE status)
            db.execSQL(
                "INSERT INTO `goals` " +
                    "(id, title, description, type, target_date, status, completed_at, created_at, updated_at) " +
                    "SELECT id, title, notes, 'MILESTONE_BASED', target_date, " +
                    "CASE WHEN status = 'COMPLETED' THEN 'COMPLETED' ELSE 'ACTIVE' END, " +
                    "completed_at, created_at, updated_at " +
                    "FROM `goals_old`",
            )

            // 8. Drop old goals table
            db.execSQL("DROP TABLE `goals_old`")

            // 9. Create new habit-specific tables
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_numeric_config` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`target_value` INTEGER NOT NULL, " +
                    "`unit` TEXT NOT NULL, " +
                    "`direction` TEXT NOT NULL, " +
                    "`increment_value` INTEGER, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_numeric_config_habit_id` ON `habit_numeric_config` (`habit_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_timer_config` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`target_duration_seconds` INTEGER NOT NULL, " +
                    "`daily_target_seconds` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_timer_config_habit_id` ON `habit_timer_config` (`habit_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_timer_sessions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`started_at` INTEGER NOT NULL, " +
                    "`paused_at` INTEGER, " +
                    "`resumed_at` INTEGER, " +
                    "`finished_at` INTEGER, " +
                    "`total_seconds` INTEGER NOT NULL, " +
                    "`is_paused` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_timer_sessions_habit_id` ON `habit_timer_sessions` (`habit_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_timer_sessions_date` ON `habit_timer_sessions` (`date`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_timer_sessions_habit_id_date` ON `habit_timer_sessions` (`habit_id`, `date`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_checklist_items` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL DEFAULT 0, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_checklist_items_habit_id` ON `habit_checklist_items` (`habit_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_checklist_progress` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`item_id` INTEGER NOT NULL, " +
                    "`completed_at` INTEGER, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`item_id`) REFERENCES `habit_checklist_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_checklist_progress_habit_id_date_item_id` ON `habit_checklist_progress` (`habit_id`, `date`, `item_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_checklist_progress_date` ON `habit_checklist_progress` (`date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_checklist_progress_item_id` ON `habit_checklist_progress` (`item_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_limit_config` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`daily_limit` INTEGER NOT NULL, " +
                    "`unit` TEXT NOT NULL, " +
                    "`allow_yes_no` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_limit_config_habit_id` ON `habit_limit_config` (`habit_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_excluded_dates` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_excluded_dates_habit_id_date` ON `habit_excluded_dates` (`habit_id`, `date`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_daily_progress` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`completion_state` TEXT NOT NULL DEFAULT 'NOT_SCHEDULED', " +
                    "`numeric_value` INTEGER, " +
                    "`timer_seconds` INTEGER, " +
                    "`note` TEXT, " +
                    "`completed_at` INTEGER, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_daily_progress_habit_id_date` ON `habit_daily_progress` (`habit_id`, `date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_daily_progress_date` ON `habit_daily_progress` (`date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_daily_progress_completion_state` ON `habit_daily_progress` (`completion_state`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habit_goal_links` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`habit_id` INTEGER NOT NULL, " +
                    "`goal_id` INTEGER NOT NULL, " +
                    "`contribution_rule` TEXT, " +
                    "FOREIGN KEY(`habit_id`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`goal_id`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_goal_links_habit_id_goal_id` ON `habit_goal_links` (`habit_id`, `goal_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_goal_links_goal_id` ON `habit_goal_links` (`goal_id`)")

            // 10. Create new goal-specific tables
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `goal_numeric_target` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`goal_id` INTEGER NOT NULL, " +
                    "`target_value` INTEGER NOT NULL, " +
                    "`current_value` INTEGER NOT NULL DEFAULT 0, " +
                    "`unit` TEXT NOT NULL, " +
                    "FOREIGN KEY(`goal_id`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_goal_numeric_target_goal_id` ON `goal_numeric_target` (`goal_id`)")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `goal_completion_target` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`goal_id` INTEGER NOT NULL, " +
                    "`target_count` INTEGER NOT NULL, " +
                    "`current_count` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`goal_id`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_goal_completion_target_goal_id` ON `goal_completion_target` (`goal_id`)")

            // 11. Migrate existing habit_check_ins to habit_daily_progress
            db.execSQL(
                "INSERT INTO `habit_daily_progress` " +
                    "(habit_id, date, completion_state, completed_at, updated_at) " +
                    "SELECT habit_id, date, 'COMPLETED', checked_in_at, checked_in_at " +
                    "FROM `habit_check_ins`",
            )

            // 12. Drop old habit_check_ins table (data migrated to habit_daily_progress)
            db.execSQL("DROP TABLE `habit_check_ins`")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
