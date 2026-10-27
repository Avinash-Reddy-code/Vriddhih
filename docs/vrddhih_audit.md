# Vrddhih Codebase Audit

**Date of Audit:** October 2026  
**Auditor:** Senior Android Engineer  
**Scope:** Habits, Quests, Goals modules, and shared foundation (Categories, Reminders/Notifications, Dashboard, Stats, Finance, Navigation, Storage).  
**Rules Applied:** Read-only inspection of active source files. No inferred/planned features reported as existing. Verbatim entity and data class definitions.

---

## 1. Project overview

### Technical Stack & Configuration
| Aspect | Implementation / Version | Citation |
|---|---|---|
| **Language** | Kotlin 2.2.10 (built into AGP) | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| **UI Toolkit** | Jetpack Compose (Compose BOM 2026.09.00), Material 3 1.4.0 | `gradle/libs.versions.toml` |
| **Architecture** | MVVM with Repository pattern; UI collects `StateFlow` via `collectAsStateWithLifecycle()` | `AGENTS.md`, `features/*` |
| **Dependency Injection** | None (Manual dependency injection via `AppContainer` in `VrddhihApplication.kt`) | `app/src/main/java/com/vrddhih/app/VrddhihApplication.kt` |
| **Min SDK / Compile / Target SDK** | minSdk 26, compileSdk 37, targetSdk 37 | `app/build.gradle.kts` |
| **Database Library & Version** | Room 2.8.5 with KSP 2.3.12 | `gradle/libs.versions.toml` |
| **Key Dependencies** | Navigation Compose 2.10.2, Lifecycle 2.11.0, Coroutines 1.10.2 | `gradle/libs.versions.toml` |
| **Offline / Network** | Strictly local-first. No network libraries, no `INTERNET` permission declared | `AndroidManifest.xml`, `AGENTS.md` |

### Package Structure (2 Levels Deep)
```
com.vrddhih.app/
├── MainActivity.kt
├── VrddhihApplication.kt
├── VrddhihApp.kt
├── core/
│   ├── data/
│   │   ├── local/          # Room database, DAOs, entities, converters, migrations
│   │   ├── habit/          # Habit scheduler, completion tracker, analytics
│   │   ├── quest/          # Quest occurrences, drafts, filters
│   │   └── repository/     # Repositories (Quests, Habits, Goals, Categories)
│   ├── navigation/         # NavHost, BottomBar, destinations
│   ├── theme/              # Color, Type, Spacing, Dimens, Radii, Brushes
│   ├── time/               # DateSelectionStateHolder
│   └── ui/                 # Shared components (Panels, Headers, FormControls, Tags)
└── features/
    ├── categories/         # Category management screen, components, ViewModel
    ├── dashboard/          # Dashboard screen, cards, mock data, ViewModel
    ├── finance/            # Finance placeholder screen
    ├── goals/              # Goals screen, cards, editor dialog, ViewModel
    ├── habits/             # Habits screen, item cards, editor dialog, ViewModel
    ├── quests/             # Quests screen, editor screen, dialogs, ViewModel
    └── statistics/         # Statistics placeholder screen
```

---

## 2. Database

- **Database Class:** `VrddhihDatabase` in `core/data/local/VrddhihDatabase.kt`
- **Current Schema Version:** `4` (`exportSchema = true`, schemas committed under `app/schemas/`)
- **Migration Strategy:** Manual migrations registered in `VrddhihMigrations.ALL`. `fallbackToDestructiveMigration()` is **NOT** enabled.
  - `MIGRATION_1_2`: Added columns to `quests` (`due_time`, `deadline`, `category`, `category_id`, `recurrence_interval`, `recurrence_days_mask`), created `categories` table with 5 built-in categories (`builtin_work`, `builtin_health`, `builtin_learning`, `builtin_personal`, `builtin_other`).
  - `MIGRATION_2_3`: Created `transactions` table (`finance_transactions`).
  - `MIGRATION_3_4`: Created tables for Goals (`goals`, `goal_milestones`, `goal_numeric_target`, `goal_completion_target`) and Habits (`habits`, `habit_check_ins`, `habit_numeric_config`, `habit_timer_config`, `habit_timer_sessions`, `habit_checklist_items`, `habit_checklist_progress`, `habit_limit_config`, `habit_excluded_dates`, `habit_daily_progress`, `habit_goal_links`).

### Table Inventory
| Table Name | Entity Class | One-Line Purpose | File Path |
|---|---|---|---|
| `quests` | `QuestEntity` | Master task/quest definitions and recurrence templates | `core/data/local/entity/QuestEntities.kt` |
| `quest_completions` | `QuestCompletionEntity` | Recorded completions per quest occurrence date | `core/data/local/entity/QuestEntities.kt` |
| `quest_occurrence_overrides` | `QuestOccurrenceOverrideEntity` | Per-date overrides (skips, edits) for recurring quest occurrences | `core/data/local/entity/QuestEntities.kt` |
| `activity_events` | `ActivityEventEntity` | Append-only event log for quest completion/reopening history | `core/data/local/entity/QuestEntities.kt` |
| `categories` | `CategoryEntity` | Shared categories/life areas used across quests, habits, and goals | `core/data/local/entity/CategoryEntity.kt` |
| `goals` | `GoalEntity` | Master goal records with title, target date, and progress type | `core/data/local/entity/GoalEntities.kt` |
| `goal_milestones` | `MilestoneEntity` | Sub-milestones for milestone-based goals | `core/data/local/entity/GoalEntities.kt` |
| `goal_numeric_target` | `GoalNumericTarget` | Target and current numeric values for numeric goals | `core/data/local/entity/GoalEntities.kt` |
| `goal_completion_target` | `GoalCompletionTarget` | Target and current count for repetition/completion-based goals | `core/data/local/entity/GoalEntities.kt` |
| `habits` | `HabitEntity` | Master habit definitions with schedule and type configs | `core/data/local/entity/HabitEntities.kt` |
| `habit_check_ins` | `HabitCheckInEntity` | Daily check-in timestamps for habits | `core/data/local/entity/HabitEntities.kt` |
| `habit_numeric_config` | `HabitNumericConfig` | Configuration for numeric habits (target, unit, direction) | `core/data/local/entity/HabitEntities.kt` |
| `habit_timer_config` | `HabitTimerConfig` | Configuration for duration/timer habits | `core/data/local/entity/HabitEntities.kt` |
| `habit_timer_sessions` | `HabitTimerSession` | Daily recorded timer durations and intervals | `core/data/local/entity/HabitEntities.kt` |
| `habit_checklist_items` | `HabitChecklistItem` | Defined sub-items for checklist habits | `core/data/local/entity/HabitEntities.kt` |
| `habit_checklist_progress` | `HabitChecklistProgress` | Per-date checklist item completion records | `core/data/local/entity/HabitEntities.kt` |
| `habit_limit_config` | `HabitLimitConfig` | Daily upper limit configuration for limit habits | `core/data/local/entity/HabitEntities.kt` |
| `habit_excluded_dates` | `HabitExcludedDate` | Specific dates excluded/skipped from a habit's schedule | `core/data/local/entity/HabitEntities.kt` |
| `habit_daily_progress` | `HabitDailyProgress` | Aggregated daily progress record per habit and date | `core/data/local/entity/HabitEntities.kt` |
| `habit_goal_links` | `HabitGoalLink` | Join table associating habits with goals and contribution rules | `core/data/local/entity/HabitEntities.kt` |
| `transactions` | `FinanceTransactionEntity` | Income and expense records | `core/data/local/entity/FinanceEntities.kt` |

### Type Converters
Defined in `core/data/local/Converters.kt`:
1. `LocalDate` ↔ `Long` (Epoch Day)
2. `LocalTime` ↔ `Long` (Nano of Day)
3. `Instant` ↔ `Long` (Epoch Millis)
4. `TaskPriority` ↔ `String` (Enum name)
5. `QuestRecurrence` ↔ `String` (Enum name)
6. `ActivityType` ↔ `String` (Enum name)
7. `TransactionType` ↔ `String` (Enum name)
8. `HabitType` ↔ `String` (Enum name)
9. `HabitScheduleType` ↔ `String` (Enum name)
10. `NumericDirection` ↔ `String` (Enum name)
11. `HabitCompletionState` ↔ `String` (Enum name)
12. `GoalType` ↔ `String` (Enum name)
13. `GoalStatus` ↔ `String` (Enum name)

---

## 3. Goals

### a) Entity definitions (verbatim)
From `app/src/main/java/com/vrddhih/app/core/data/local/entity/GoalEntities.kt`:

```kotlin
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("status"), Index("category_id"), Index("target_date")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val type: GoalType = GoalType.MILESTONE_BASED,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "start_date") val startDate: LocalDate? = null,
    @ColumnInfo(name = "target_date") val targetDate: LocalDate? = null,
    @ColumnInfo(defaultValue = "ACTIVE") val status: GoalStatus = GoalStatus.ACTIVE,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id")],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    val title: String,
    @ColumnInfo(defaultValue = "0") val position: Int = 0,
    /** Null until the milestone is completed. */
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

@Entity(
    tableName = "goal_numeric_target",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id", unique = true)],
)
data class GoalNumericTarget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "target_value") val targetValue: Long,
    @ColumnInfo(name = "current_value") val currentValue: Long = 0,
    val unit: String,
)

@Entity(
    tableName = "goal_completion_target",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goal_id", unique = true)],
)
data class GoalCompletionTarget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "target_count") val targetCount: Int,
    @ColumnInfo(name = "current_count") val currentCount: Int = 0,
)

data class GoalWithMilestones(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val milestones: List<MilestoneEntity>,
)

data class GoalWithNumericTarget(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val numericTarget: GoalNumericTarget?,
)

data class GoalWithCompletionTarget(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val completionTarget: GoalCompletionTarget?,
)

// Declared in core/data/local/dao/GoalDao.kt:
data class GoalWithAllConfigs(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val milestones: List<MilestoneEntity>,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val numericTarget: GoalNumericTarget?,
    @Relation(parentColumn = "id", entityColumn = "goal_id")
    val completionTarget: GoalCompletionTarget?,
)
```

Also related from `core/data/local/entity/HabitEntities.kt`:
```kotlin
@Entity(
    tableName = "habit_goal_links",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "goal_id"], unique = true), Index("goal_id")],
)
data class HabitGoalLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "contribution_rule") val contributionRule: String? = null,
)
```

### b) Enums / Types
From `core/data/local/entity/GoalEntities.kt`:
```kotlin
enum class GoalType { MILESTONE_BASED, NUMERIC_TARGET, COMPLETION_BASED }

enum class GoalStatus { NOT_STARTED, ACTIVE, PAUSED, COMPLETED, ARCHIVED }
```

### c) DAO methods (signatures only)
From `core/data/local/dao/GoalDao.kt`:
```kotlin
@Insert
suspend fun insertGoal(goal: GoalEntity): Long

@Update
suspend fun updateGoal(goal: GoalEntity)

@Delete
suspend fun deleteGoal(goal: GoalEntity)

@Query("SELECT * FROM goals WHERE id = :id")
suspend fun getGoal(id: Long): GoalEntity?

@Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
fun observeActiveGoals(): Flow<List<GoalEntity>>

@Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
suspend fun getActiveGoals(): List<GoalEntity>

@Query("SELECT * FROM goals WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
fun observeCompletedGoals(): Flow<List<GoalEntity>>

@Query("SELECT * FROM goals WHERE status = 'ARCHIVED' ORDER BY updated_at DESC")
fun observeArchivedGoals(): Flow<List<GoalEntity>>

@Transaction
@Query("SELECT * FROM goals WHERE id = :id")
fun observeGoalWithMilestones(id: Long): Flow<GoalWithMilestones?>

@Transaction
@Query("SELECT * FROM goals WHERE id = :id")
suspend fun getGoalWithMilestones(id: Long): GoalWithMilestones?

@Insert
suspend fun insertMilestone(milestone: MilestoneEntity): Long

@Query("UPDATE goal_milestones SET completed_at = :completedAt WHERE id = :id")
suspend fun setMilestoneCompletedAt(id: Long, completedAt: Instant?): Int

@Delete
suspend fun deleteMilestone(milestone: MilestoneEntity)

@Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY position, id")
suspend fun getMilestones(goalId: Long): List<MilestoneEntity>

@Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY position, id")
fun observeMilestones(goalId: Long): Flow<List<MilestoneEntity>>

@Query("DELETE FROM goal_milestones WHERE goal_id = :goalId")
suspend fun deleteMilestonesForGoal(goalId: Long): Int

@Insert
suspend fun insertNumericTarget(target: GoalNumericTarget): Long

@Update
suspend fun updateNumericTarget(target: GoalNumericTarget)

@Query("SELECT * FROM goal_numeric_target WHERE goal_id = :goalId")
suspend fun getNumericTarget(goalId: Long): GoalNumericTarget?

@Query("SELECT * FROM goal_numeric_target WHERE goal_id = :goalId")
fun observeNumericTarget(goalId: Long): Flow<GoalNumericTarget?>

@Query("UPDATE goal_numeric_target SET current_value = :currentValue WHERE goal_id = :goalId")
suspend fun updateNumericTargetCurrentValue(goalId: Long, currentValue: Long): Int

@Delete
suspend fun deleteNumericTarget(target: GoalNumericTarget)

@Insert
suspend fun insertCompletionTarget(target: GoalCompletionTarget): Long

@Update
suspend fun updateCompletionTarget(target: GoalCompletionTarget)

@Query("SELECT * FROM goal_completion_target WHERE goal_id = :goalId")
suspend fun getCompletionTarget(goalId: Long): GoalCompletionTarget?

@Query("SELECT * FROM goal_completion_target WHERE goal_id = :goalId")
fun observeCompletionTarget(goalId: Long): Flow<GoalCompletionTarget?>

@Query("UPDATE goal_completion_target SET current_count = :currentCount WHERE goal_id = :goalId")
suspend fun updateCompletionTargetCurrentCount(goalId: Long, currentCount: Int): Int

@Delete
suspend fun deleteCompletionTarget(target: GoalCompletionTarget)

@Transaction
@Query("SELECT * FROM goals WHERE id = :id")
fun observeGoalWithNumericTarget(id: Long): Flow<com.vrddhih.app.core.data.local.entity.GoalWithNumericTarget?>

@Transaction
@Query("SELECT * FROM goals WHERE id = :id")
fun observeGoalWithCompletionTarget(id: Long): Flow<com.vrddhih.app.core.data.local.entity.GoalWithCompletionTarget?>

@Transaction
@Query("SELECT * FROM goals WHERE id = :id")
suspend fun getGoalWithAllConfigs(id: Long): GoalWithAllConfigs?

@Transaction
@Query("SELECT * FROM goals ORDER BY target_date IS NULL, target_date, id")
fun observeAllGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>>

@Transaction
@Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date IS NULL, target_date, id")
fun observeActiveGoalsWithAllConfigs(): Flow<List<GoalWithAllConfigs>>
```

### d) How goal progress is calculated
- **Implementation citations:**
  1. `GoalCard.kt` lines 82–104 (in `com.vrddhih.app.features.goals.components.GoalCard`)
  2. `DashboardViewModel.kt` lines 120–138 (in `com.vrddhih.app.features.dashboard.DashboardViewModel`)
- **Calculation Logic:**
  - For `GoalType.MILESTONE_BASED`:
    $$\text{progress} = \frac{\text{count of milestones where } completedAt \neq null}{\text{total milestones size}}$$
    If total milestones count is 0, progress is `0f`. Coerced to `0f..1f`.
  - For `GoalType.NUMERIC_TARGET`:
    $$\text{progress} = \frac{currentValue}{targetValue}$$
    If `targetValue <= 0`, progress is `0f`. Coerced to `0f..1f`.
  - For `GoalType.COMPLETION_BASED`:
    $$\text{progress} = \frac{currentCount}{targetCount}$$
    If `targetCount <= 0`, progress is `0f`. Coerced to `0f..1f`.
- **Habit-driven progress:** NOT IMPLEMENTED (no calculation links habit completions to goal progress).

### e) Screens / Composables / Fragments
| Component | Function / Composable | File Path |
|---|---|---|
| Screen Container & Route | `GoalsRoute`, `GoalsScreen` | `features/goals/GoalsScreen.kt` |
| Goal Card List Item | `GoalCard` | `features/goals/components/GoalCard.kt` |
| Goal Editor Dialog (Create/Edit) | `GoalEditorDialog` | `features/goals/components/GoalEditorDialog.kt` |
| Delete Confirmation Dialog | `GoalDeleteDialog` | `features/goals/components/GoalDeleteDialog.kt` |
| Detail Screen / Dialog | NOT IMPLEMENTED (No dedicated detail screen; `GoalCard` expands inline) | `features/goals/components/GoalCard.kt` |

### f) Creation form fields persisted vs UI-only
From `features/goals/components/GoalEditorDialog.kt` and `GoalRepository.kt`:
- **Persisted:**
  - `title`: `GoalEntity.title`
  - `description`: `GoalEntity.description`
  - `type`: `GoalEntity.type`
  - `categoryId`: `GoalEntity.categoryId`
  - `targetDate`: `GoalEntity.targetDate`
  - `status`: Defaulted to `GoalStatus.ACTIVE`
  - If `MILESTONE_BASED`: list of non-empty milestone title strings inserted into `goal_milestones`
  - If `NUMERIC_TARGET`: `target_value` and `unit` inserted into `goal_numeric_target` (`current_value = 0`)
  - If `COMPLETION_BASED`: `target_count` inserted into `goal_completion_target` (`current_count = 0`)
- **UI-Only / Not Persisted:**
  - `GoalEntity.startDate` is nullable in DB schema, but the editor dialog supplies `null` (not editable in form).
  - Milestone text input errors (`validationError` states in dialog).
  - Target value / count text parsing error states.

### g) Reminder / deadline handling
- **Reminders / Notifications:** NOT IMPLEMENTED (no alarms, no reminders).
- **Deadlines:** `targetDate` (`LocalDate?`) is persisted and indexed. Used solely for display in `GoalCard` and ordering queries (`ORDER BY target_date IS NULL, target_date, id`).

---

## 4. Habits

### a) Entity definitions (verbatim)
From `app/src/main/java/com/vrddhih/app/core/data/local/entity/HabitEntities.kt`:

```kotlin
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("category_id"), Index("start_date")],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val type: HabitType = HabitType.YES_NO,
    @ColumnInfo(defaultValue = "DAILY") val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    @ColumnInfo(name = "schedule_interval", defaultValue = "1") val scheduleInterval: Int = 1,
    @ColumnInfo(name = "schedule_days_mask", defaultValue = "127") val scheduleDaysMask: Int = EVERY_DAY_MASK,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "icon_key") val iconKey: String? = null,
    @ColumnInfo(name = "color_argb") val colorArgb: Int? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "xp_per_completion", defaultValue = "5") val xpPerCompletion: Int = 5,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

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

@Entity(
    tableName = "habit_numeric_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitNumericConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "target_value") val targetValue: Long,
    val unit: String,
    val direction: NumericDirection,
    @ColumnInfo(name = "increment_value") val incrementValue: Long? = null,
)

@Entity(
    tableName = "habit_timer_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitTimerConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "target_duration_seconds") val targetDurationSeconds: Int,
    @ColumnInfo(name = "daily_target_seconds") val dailyTargetSeconds: Int,
)

@Entity(
    tableName = "habit_timer_sessions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id"), Index("date"), Index(value = ["habit_id", "date"], unique = true)],
)
data class HabitTimerSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "started_at") val startedAt: Instant,
    @ColumnInfo(name = "paused_at") val pausedAt: Instant? = null,
    @ColumnInfo(name = "resumed_at") val resumedAt: Instant? = null,
    @ColumnInfo(name = "finished_at") val finishedAt: Instant? = null,
    @ColumnInfo(name = "total_seconds") val totalSeconds: Int,
    @ColumnInfo(name = "is_paused", defaultValue = "0") val isPaused: Boolean = false,
)

@Entity(
    tableName = "habit_checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id")],
)
data class HabitChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val title: String,
    @ColumnInfo(defaultValue = "0") val position: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

@Entity(
    tableName = "habit_checklist_progress",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HabitChecklistItem::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date", "item_id"], unique = true), Index("date"), Index("item_id")],
)
data class HabitChecklistProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "item_id") val itemId: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
)

@Entity(
    tableName = "habit_limit_config",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id", unique = true)],
)
data class HabitLimitConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "daily_limit") val dailyLimit: Long,
    val unit: String,
    @ColumnInfo(name = "allow_yes_no", defaultValue = "0") val allowYesNo: Boolean = false,
)

@Entity(
    tableName = "habit_excluded_dates",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true)],
)
data class HabitExcludedDate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
)

@Entity(
    tableName = "habit_daily_progress",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "date"], unique = true), Index("date"), Index("completion_state")],
)
data class HabitDailyProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(name = "completion_state") val completionState: HabitCompletionState = HabitCompletionState.NOT_SCHEDULED,
    @ColumnInfo(name = "numeric_value") val numericValue: Long? = null,
    @ColumnInfo(name = "timer_seconds") val timerSeconds: Int? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "habit_goal_links",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "goal_id"], unique = true), Index("goal_id")],
)
data class HabitGoalLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "contribution_rule") val contributionRule: String? = null,
)

data class HabitWithConfigs(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val numericConfig: HabitNumericConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val timerConfig: HabitTimerConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val limitConfig: HabitLimitConfig?,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val checklistItems: List<HabitChecklistItem> = emptyList(),
)
```

### b) Enums / Types
From `core/data/local/entity/HabitEntities.kt`:
```kotlin
enum class HabitType { YES_NO, NUMERIC, TIMER, CHECKLIST, LIMIT }

enum class HabitScheduleType {
    DAILY,
    EVERY_N_DAYS,
    WEEKLY_FREQUENCY,
    SELECTED_WEEKDAYS,
}

enum class NumericDirection { AT_LEAST, AT_MOST }

enum class HabitCompletionState { COMPLETED, PARTIALLY_COMPLETED, MISSED, SKIPPED, NOT_SCHEDULED }
```

### c) DAO methods (signatures only)
From `core/data/local/dao/HabitDao.kt`:
```kotlin
@Insert
suspend fun insertHabit(habit: HabitEntity): Long

@Update
suspend fun updateHabit(habit: HabitEntity)

@Delete
suspend fun deleteHabit(habit: HabitEntity)

@Query("SELECT * FROM habits WHERE id = :id")
suspend fun getHabit(id: Long): HabitEntity?

@Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY name COLLATE NOCASE, id")
fun observeActiveHabits(): Flow<List<HabitEntity>>

@Query("SELECT * FROM habits WHERE is_archived = 1 ORDER BY name COLLATE NOCASE, id")
fun observeArchivedHabits(): Flow<List<HabitEntity>>

@Query("SELECT * FROM habits WHERE category_id = :categoryId AND is_archived = 0 ORDER BY name COLLATE NOCASE, id")
fun observeHabitsByCategory(categoryId: String): Flow<List<HabitEntity>>

@Query("SELECT * FROM habits WHERE id IN (:ids)")
suspend fun getHabitsByIds(ids: List<Long>): List<HabitEntity>

@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertCheckIn(checkIn: HabitCheckInEntity): Long

@Query("DELETE FROM habit_check_ins WHERE habit_id = :habitId AND date = :date")
suspend fun deleteCheckIn(habitId: Long, date: LocalDate): Int

@Query("SELECT * FROM habit_check_ins WHERE date = :date")
fun observeCheckInsOn(date: LocalDate): Flow<List<HabitCheckInEntity>>

@Query("SELECT * FROM habit_check_ins WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
fun observeCheckInsBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitCheckInEntity>>

@Query("SELECT COUNT(*) FROM habit_daily_progress WHERE date = :date AND completion_state = 'COMPLETED'")
fun observeCheckInCountOn(date: LocalDate): Flow<Int>

@Insert
suspend fun insertDailyProgress(progress: HabitDailyProgress): Long

@Update
suspend fun updateDailyProgress(progress: HabitDailyProgress)

@Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
suspend fun getDailyProgress(habitId: Long, date: LocalDate): HabitDailyProgress?

@Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
fun observeDailyProgress(habitId: Long, date: LocalDate): Flow<HabitDailyProgress?>

@Query("SELECT * FROM habit_daily_progress WHERE date = :date")
fun observeDailyProgressOn(date: LocalDate): Flow<List<HabitDailyProgress>>

@Query("SELECT * FROM habit_daily_progress WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
fun observeDailyProgressBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitDailyProgress>>

@Query("DELETE FROM habit_daily_progress WHERE habit_id = :habitId AND date = :date")
suspend fun deleteDailyProgress(habitId: Long, date: LocalDate): Int

@Insert
suspend fun insertNumericConfig(config: HabitNumericConfig): Long

@Update
suspend fun updateNumericConfig(config: HabitNumericConfig)

@Query("SELECT * FROM habit_numeric_config WHERE habit_id = :habitId")
suspend fun getNumericConfig(habitId: Long): HabitNumericConfig?

@Query("SELECT * FROM habit_numeric_config WHERE habit_id = :habitId")
fun observeNumericConfig(habitId: Long): Flow<HabitNumericConfig?>

@Delete
suspend fun deleteNumericConfig(config: HabitNumericConfig)

@Insert
suspend fun insertTimerConfig(config: HabitTimerConfig): Long

@Update
suspend fun updateTimerConfig(config: HabitTimerConfig)

@Query("SELECT * FROM habit_timer_config WHERE habit_id = :habitId")
suspend fun getTimerConfig(habitId: Long): HabitTimerConfig?

@Query("SELECT * FROM habit_timer_config WHERE habit_id = :habitId")
fun observeTimerConfig(habitId: Long): Flow<HabitTimerConfig?>

@Delete
suspend fun deleteTimerConfig(config: HabitTimerConfig)

@Insert
suspend fun insertTimerSession(session: HabitTimerSession): Long

@Update
suspend fun updateTimerSession(session: HabitTimerSession)

@Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
suspend fun getTimerSession(habitId: Long, date: LocalDate): HabitTimerSession?

@Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
fun observeTimerSession(habitId: Long, date: LocalDate): Flow<HabitTimerSession?>

@Query("SELECT * FROM habit_timer_sessions WHERE habit_id = :habitId AND date BETWEEN :start AND :end ORDER BY date")
fun observeTimerSessionsBetween(habitId: Long, start: LocalDate, end: LocalDate): Flow<List<HabitTimerSession>>

@Query("DELETE FROM habit_timer_sessions WHERE habit_id = :habitId AND date = :date")
suspend fun deleteTimerSession(habitId: Long, date: LocalDate): Int

@Insert
suspend fun insertChecklistItem(item: HabitChecklistItem): Long

@Update
suspend fun updateChecklistItem(item: HabitChecklistItem)

@Delete
suspend fun deleteChecklistItem(item: HabitChecklistItem)

@Query("SELECT * FROM habit_checklist_items WHERE habit_id = :habitId ORDER BY position, id")
suspend fun getChecklistItems(habitId: Long): List<HabitChecklistItem>

@Query("SELECT * FROM habit_checklist_items WHERE habit_id = :habitId ORDER BY position, id")
fun observeChecklistItems(habitId: Long): Flow<List<HabitChecklistItem>>

@Query("DELETE FROM habit_checklist_items WHERE habit_id = :habitId")
suspend fun deleteChecklistItemsForHabit(habitId: Long): Int

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertChecklistProgress(progress: HabitChecklistProgress): Long

@Query("SELECT * FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
suspend fun getChecklistProgress(habitId: Long, date: LocalDate): List<HabitChecklistProgress>

@Query("SELECT * FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
fun observeChecklistProgress(habitId: Long, date: LocalDate): Flow<List<HabitChecklistProgress>>

@Query("DELETE FROM habit_checklist_progress WHERE habit_id = :habitId AND date = :date")
suspend fun deleteChecklistProgress(habitId: Long, date: LocalDate): Int

@Insert
suspend fun insertLimitConfig(config: HabitLimitConfig): Long

@Update
suspend fun updateLimitConfig(config: HabitLimitConfig)

@Query("SELECT * FROM habit_limit_config WHERE habit_id = :habitId")
suspend fun getLimitConfig(habitId: Long): HabitLimitConfig?

@Query("SELECT * FROM habit_limit_config WHERE habit_id = :habitId")
fun observeLimitConfig(habitId: Long): Flow<HabitLimitConfig?>

@Delete
suspend fun deleteLimitConfig(config: HabitLimitConfig)

@Insert
suspend fun insertExcludedDate(excludedDate: HabitExcludedDate): Long

@Query("DELETE FROM habit_excluded_dates WHERE habit_id = :habitId AND date = :date")
suspend fun deleteExcludedDate(habitId: Long, date: LocalDate): Int

@Query("SELECT * FROM habit_excluded_dates WHERE habit_id = :habitId")
suspend fun getExcludedDates(habitId: Long): List<HabitExcludedDate>

@Query("SELECT * FROM habit_excluded_dates WHERE habit_id = :habitId")
fun observeExcludedDates(habitId: Long): Flow<List<HabitExcludedDate>>

@Query("DELETE FROM habit_excluded_dates WHERE habit_id = :habitId")
suspend fun deleteExcludedDatesForHabit(habitId: Long): Int

@Insert
suspend fun insertHabitGoalLink(link: HabitGoalLink): Long

@Query("DELETE FROM habit_goal_links WHERE habit_id = :habitId AND goal_id = :goalId")
suspend fun deleteHabitGoalLink(habitId: Long, goalId: Long): Int

@Query("SELECT * FROM habit_goal_links WHERE habit_id = :habitId")
suspend fun getHabitGoalLinks(habitId: Long): List<HabitGoalLink>

@Query("SELECT * FROM habit_goal_links WHERE habit_id = :habitId")
fun observeHabitGoalLinks(habitId: Long): Flow<List<HabitGoalLink>>

@Query("SELECT * FROM habit_goal_links WHERE goal_id = :goalId")
suspend fun getHabitsForGoal(goalId: Long): List<HabitGoalLink>

@Query("DELETE FROM habit_goal_links WHERE habit_id = :habitId")
suspend fun deleteHabitGoalLinksForHabit(habitId: Long): Int

@Transaction
@Query("SELECT * FROM habits WHERE id = :id")
suspend fun getHabitWithConfigs(id: Long): HabitWithConfigs?

@Transaction
@Query("SELECT * FROM habits WHERE id = :id")
fun observeHabitWithConfigs(id: Long): Flow<HabitWithConfigs?>

@Transaction
@Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY name COLLATE NOCASE, id")
fun observeActiveHabitsWithConfigs(): Flow<List<HabitWithConfigs>>

@Query("SELECT * FROM habit_checklist_progress WHERE date = :date")
fun observeChecklistProgressOn(date: LocalDate): Flow<List<HabitChecklistProgress>>
```

### d) How habit progress is calculated
- **Implementation:** `HabitCompletionTracker.kt` (in `com.vrddhih.app.core.data.habit.HabitCompletionTracker`).
- **Logic:**
  - `YES_NO`: Checks if `HabitCheckInEntity` exists for the date or `HabitDailyProgress.completionState == COMPLETED`.
  - `NUMERIC`: Evaluates `currentValue` against `targetValue` based on `NumericDirection`:
    - `AT_LEAST`: Completed if `currentValue >= targetValue`. If `currentValue > 0`, marked `PARTIALLY_COMPLETED`.
    - `AT_MOST`: Completed if `currentValue <= targetValue`. If `currentValue > targetValue`, marked `MISSED`.
  - `TIMER`: Completed if `currentSeconds >= targetDurationSeconds`.
  - `CHECKLIST`: Completed if count of completed items in `HabitChecklistProgress` equals total items in `HabitChecklistItem`. If $> 0$ but $< \text{total}$, marked `PARTIALLY_COMPLETED`.
  - `LIMIT`: Completed if `currentValue <= dailyLimit`.

### e) Screens / Composables
| Component | Function / Composable | File Path |
|---|---|---|
| Screen Container | `HabitsScreen` | `features/habits/HabitsScreen.kt` |
| Habit Item Card | `HabitItemCard` | `features/habits/components/HabitItemCard.kt` |
| Habit Editor Dialog | `HabitEditorDialog` | `features/habits/components/HabitEditorDialog.kt` |
| Habit Delete Dialog | `HabitDeleteDialog` | `features/habits/components/HabitDeleteDialog.kt` |

### f) Creation form fields persisted vs UI-only
- **Persisted:** `name`, `description`, `type`, `scheduleType`, `scheduleInterval` (for `EVERY_N_DAYS`), `scheduleDaysMask` (for `SELECTED_WEEKDAYS`), `startDate`, `endDate`, `categoryId`, `colorArgb`. Also type-specific configs: `HabitNumericConfig` (target, unit, direction), `HabitTimerConfig` (target duration), `HabitLimitConfig` (daily limit, unit), and list of `HabitChecklistItem` (titles, positions).
- **UI-Only / Not Persisted:** Validation error flags, temporary interval/numeric text string buffers in dialog state.

### g) Reminder / deadline handling
- **Reminders / Notifications:** NOT IMPLEMENTED (no alarms or notification scheduling exists).
- **Deadlines:** `endDate: LocalDate?` is stored in `HabitEntity` and used by `HabitScheduler.isScheduledOn()` (`if (endDate != null && date.isAfter(endDate)) return false`).

### Supported Habit Types
| Habit Type | Status | How it is logged |
|---|---|---|
| **YES_NO** | IMPLEMENTED | Toggles row in `habit_check_ins` and upserts `habit_daily_progress.completionState = COMPLETED / MISSED` |
| **NUMERIC** | IMPLEMENTED | Upserts `habit_daily_progress.numericValue` and compares against `HabitNumericConfig.targetValue` |
| **TIMER** | PARTIAL | Config stored in `habit_timer_config`; updates `habit_daily_progress.timerSeconds` and inserts into `habit_timer_sessions`. In UI: simple start/stop button without background service |
| **CHECKLIST** | IMPLEMENTED | Checklist item defined in `habit_checklist_items`; completion toggled in `habit_checklist_progress` with `completed_at: Instant?` |
| **LIMIT** | IMPLEMENTED | Config stored in `habit_limit_config`; daily progress logged to `habit_daily_progress.numericValue` and compared against `dailyLimit` |

### Supported Schedules
From `HabitScheduler.kt` and `HabitEntities.kt`:
1. `DAILY`: Evaluates true every calendar day after `startDate`.
2. `EVERY_N_DAYS`: Evaluates true if `ChronoUnit.DAYS.between(startDate, date) % scheduleInterval == 0L`.
3. `WEEKLY_FREQUENCY`: Evaluates target times per week (partial in scheduler).
4. `SELECTED_WEEKDAYS`: Evaluates `(scheduleDaysMask and date.dayOfWeek.maskBit()) != 0`.

### Streak & Consistency Calculation
- **Streak:** Computed by `HabitAnalytics.calculateStreak()` in `core/data/habit/HabitAnalytics.kt`. Calculates `current` and `best` streaks by iterating through consecutive scheduled dates and checking if each was completed.
- **Strength / Consistency Score:** NOT IMPLEMENTED (no decay algorithm, soft misses, or consistency scoring exists).
- **Note:** `HabitAnalytics.calculateStatistics()` and `calculateStreak()` are **NOT** wired to any UI composable; `HabitsScreen` does not show streaks.

### Date Handling
- Stored as `java.time.LocalDate` (converted to `Long` epoch day in Room via `Converters.kt`).
- Day-start offset: NOT IMPLEMENTED (always cuts over at midnight local time).
- Timezone/DST: Uses epoch day without timestamps, preventing DST drift across calendar dates.
- Backdated logging: IMPLEMENTED in `HabitRepository` (all log/progress methods take `date: LocalDate`) and UI (`HabitsScreen` supports date selection).

### Timer Implementation
- **Foreground Service:** NOT IMPLEMENTED. No foreground service, notification, or background worker exists in `AndroidManifest.xml` or code. Timer logging is in-memory in the UI and written to Room on stop.

---

## 5. Quests

### a) Entity definitions (verbatim)
From `app/src/main/java/com/vrddhih/app/core/data/local/entity/QuestEntities.kt`:

```kotlin
@Entity(
    tableName = "quests",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            // No cascade: deleting a category must not delete tasks. Repository reassigns to Other first.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("goal_id"), Index("start_date"), Index("category_id")],
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** User-facing "description". */
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "MEDIUM") val priority: TaskPriority = TaskPriority.MEDIUM,
    @ColumnInfo(name = "xp_reward", defaultValue = "10") val xpReward: Int = 10,
    /** The scheduled day for one-off quests, or the first occurrence for recurring quests. */
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(defaultValue = "NONE") val recurrence: QuestRecurrence = QuestRecurrence.NONE,
    /** Last day (inclusive) a recurring quest can occur. Null means no end. Ignored for one-off quests. */
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "goal_id") val goalId: Long? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    // Added in schema v2.
    @ColumnInfo(name = "due_time") val dueTime: LocalTime? = null,
    /** Last acceptable day for a one-off quest. Overdue = not completed and (deadline ?: startDate) < today. */
    val deadline: LocalDate? = null,
    /**
     * Legacy plain-text category field from schema v2. Kept to avoid destructive migration.
     * All new saves use [categoryId] instead. Never read for display.
     */
    @ColumnInfo(defaultValue = "") val category: String = "",
    /** FK to [CategoryEntity.id]. Null is treated as the "Other" built-in category at read time. */
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "recurrence_interval", defaultValue = "1") val recurrenceInterval: Int = 1,
    /** ISO weekday bits (Monday = bit 0) used by [QuestRecurrence.WEEKDAYS]. */
    @ColumnInfo(name = "recurrence_days_mask", defaultValue = "0") val recurrenceDaysMask: Int = 0,
)

@Entity(
    tableName = "quest_completions",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["quest_id", "occurrence_date"], unique = true), Index("occurrence_date")],
)
data class QuestCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quest_id") val questId: Long,
    /** The scheduled day this completion belongs to, which may differ from the day it was ticked off. */
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "completed_at") val completedAt: Instant,
    @ColumnInfo(name = "xp_awarded", defaultValue = "0") val xpAwarded: Int = 0,
)

@Entity(
    tableName = "quest_occurrence_overrides",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["quest_id", "occurrence_date"], unique = true), Index("occurrence_date")],
)
data class QuestOccurrenceOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quest_id") val questId: Long,
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "is_skipped", defaultValue = "0") val isSkipped: Boolean = false,
    val title: String? = null,
    val notes: String? = null,
    val priority: TaskPriority? = null,
    @ColumnInfo(name = "due_time") val dueTime: LocalTime? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "activity_events",
    indices = [Index("occurrence_date"), Index("quest_id")],
)
data class ActivityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ActivityType,
    @ColumnInfo(name = "quest_id") val questId: Long?,
    @ColumnInfo(name = "quest_title") val questTitle: String,
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
)
```

### b) Enums / Types
From `core/data/local/entity/QuestEntities.kt`:
```kotlin
enum class TaskPriority { CRITICAL, HIGH, MEDIUM, LOW }

enum class QuestRecurrence {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKLY,
    MONTHLY,
}

enum class ActivityType { QUEST_COMPLETED, QUEST_REOPENED }
```
From `core/data/quest/QuestDraft.kt`:
```kotlin
enum class QuestDraftError {
    TITLE_REQUIRED,
    TITLE_TOO_LONG,
    DEADLINE_BEFORE_DATE,
    END_BEFORE_START,
    INTERVAL_OUT_OF_RANGE,
    NO_WEEKDAYS,
}

enum class QuestScope { OCCURRENCE, FUTURE, ALL }
```
From `core/data/quest/QuestBook.kt`:
```kotlin
enum class QuestFilter { ALL, TODAY, UPCOMING, OVERDUE, COMPLETED }

enum class QuestSort { DEADLINE, PRIORITY, CREATED }
```

### c) DAO methods (signatures only)
From `core/data/local/dao/QuestDao.kt`:
```kotlin
@Insert
suspend fun insertQuest(quest: QuestEntity): Long

@Update
suspend fun updateQuest(quest: QuestEntity)

@Delete
suspend fun deleteQuest(quest: QuestEntity)

@Query("DELETE FROM quests WHERE id = :id")
suspend fun deleteQuestById(id: Long): Int

@Query("UPDATE quests SET is_archived = 1, updated_at = :now WHERE id = :id")
suspend fun archiveQuest(id: Long, now: Instant)

@Query("SELECT * FROM quests WHERE id = :id")
suspend fun getQuest(id: Long): QuestEntity?

@Query("SELECT * FROM quests WHERE id = :id")
fun observeQuest(id: Long): Flow<QuestEntity?>

@Query("SELECT * FROM quests WHERE is_archived = 0 ORDER BY start_date, id")
fun observeActiveQuests(): Flow<List<QuestEntity>>

@Query("""
    SELECT * FROM quests
    WHERE is_archived = 0
      AND start_date <= :date
      AND (recurrence != 'NONE' OR start_date = :date)
      AND (recurrence = 'NONE' OR end_date IS NULL OR end_date >= :date)
    ORDER BY id
""")
fun observeQuestCandidatesOn(date: LocalDate): Flow<List<QuestEntity>>

@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertCompletion(completion: QuestCompletionEntity): Long

@Query("DELETE FROM quest_completions WHERE quest_id = :questId AND occurrence_date = :date")
suspend fun deleteCompletion(questId: Long, date: LocalDate): Int

@Query("DELETE FROM quest_completions WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun deleteCompletionsFrom(questId: Long, from: LocalDate): Int

@Query("UPDATE quest_completions SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun moveCompletionsFrom(questId: Long, from: LocalDate, newQuestId: Long)

@Query("SELECT * FROM quest_completions WHERE occurrence_date = :date ORDER BY completed_at")
fun observeCompletionsOn(date: LocalDate): Flow<List<QuestCompletionEntity>>

@Query("SELECT * FROM quest_completions ORDER BY completed_at DESC")
fun observeAllCompletions(): Flow<List<QuestCompletionEntity>>

@Query("SELECT * FROM quest_completions WHERE quest_id = :questId ORDER BY occurrence_date")
suspend fun getCompletionsFor(questId: Long): List<QuestCompletionEntity>

@Query("SELECT COUNT(*) FROM quest_completions WHERE occurrence_date = :date")
fun observeCompletionCountOn(date: LocalDate): Flow<Int>

@Upsert
suspend fun upsertOverride(override: QuestOccurrenceOverrideEntity)

@Query("SELECT * FROM quest_occurrence_overrides WHERE quest_id = :questId AND occurrence_date = :date")
suspend fun getOverride(questId: Long, date: LocalDate): QuestOccurrenceOverrideEntity?

@Query("SELECT * FROM quest_occurrence_overrides WHERE occurrence_date = :date")
fun observeOverridesOn(date: LocalDate): Flow<List<QuestOccurrenceOverrideEntity>>

@Query("SELECT * FROM quest_occurrence_overrides")
fun observeAllOverrides(): Flow<List<QuestOccurrenceOverrideEntity>>

@Query("DELETE FROM quest_occurrence_overrides WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun deleteOverridesFrom(questId: Long, from: LocalDate)

@Query("UPDATE quest_occurrence_overrides SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun moveOverridesFrom(questId: Long, from: LocalDate, newQuestId: Long)

@Insert
suspend fun insertActivity(event: ActivityEventEntity): Long

@Query("SELECT * FROM activity_events WHERE occurrence_date = :date ORDER BY occurred_at, id")
fun observeActivityOn(date: LocalDate): Flow<List<ActivityEventEntity>>

@Query("SELECT * FROM activity_events WHERE quest_id = :questId ORDER BY occurred_at, id")
suspend fun getActivityFor(questId: Long): List<ActivityEventEntity>

@Query("DELETE FROM activity_events WHERE quest_id = :questId")
suspend fun deleteActivityFor(questId: Long)

@Query("DELETE FROM activity_events WHERE quest_id = :questId AND occurrence_date = :date")
suspend fun deleteActivityOn(questId: Long, date: LocalDate)

@Query("DELETE FROM activity_events WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun deleteActivityFrom(questId: Long, from: LocalDate)

@Query("UPDATE activity_events SET quest_id = :newQuestId WHERE quest_id = :questId AND occurrence_date >= :from")
suspend fun moveActivityFrom(questId: Long, from: LocalDate, newQuestId: Long)
```

### d) How quest progress / completion is calculated
- Quests are discrete occurrence items; progress is binary per occurrence.
- `QuestOccurrence.isCompleted`: Derived dynamically as `completion != null` where `completion` is queried from `quest_completions` for `(quest_id, occurrence_date)`.
- Reopening deletes the `QuestCompletionEntity` and appends an `ActivityEventEntity(type = ActivityType.QUEST_REOPENED)`.

### e) Screens / Composables
| Component | Function / Composable | File Path |
|---|---|---|
| Screen Container & Route | `QuestsRoute`, `QuestsScreen` | `features/quests/QuestsScreen.kt` |
| Quest Item Row | `QuestItemRow` | `features/quests/components/QuestItemRow.kt` |
| Full-Screen Editor | `QuestEditorScreen` | `features/quests/QuestEditorScreen.kt` |
| Quest Detail Dialog | `QuestDetailDialog` | `features/quests/components/QuestDetailDialog.kt` |
| Quest Delete Dialog | `QuestDeleteDialog` | `features/quests/components/QuestDeleteDialog.kt` |

### f) Creation form fields persisted vs UI-only
- **Persisted:** `title`, `notes` (description), `priority` (`TaskPriority`), `startDate` (scheduledDate), `dueTime`, `deadline`, `categoryId`, `recurrence` (`QuestRecurrence`), `recurrenceInterval`, `recurrenceDaysMask`, `endDate`.
- **UI-Only / Not Persisted:**
  - `intervalText`: temporary text buffer in `QuestDraft` during editing.
  - `xpReward`: Defaults to 10 in entity, not exposed in `QuestEditorScreen` UI.
  - `goalId`: Foreign key column exists on `QuestEntity`, but `QuestEditorScreen` does **NOT** expose a goal-selector field.

### g) Reminder / deadline handling
- **Reminders / Notifications:** NOT IMPLEMENTED (no alarms or notification scheduling exists).
- **Deadlines:** `deadline: LocalDate?` and `dueTime: LocalTime?` are stored and displayed. For one-off quests, `effectiveDeadline` is `deadline ?: startDate`.

### Quest Kinds / Types
- `QuestRecurrence`: `NONE`, `DAILY`, `WEEKDAYS`, `WEEKLY`, `MONTHLY`.
- "Main" / "Side" quest kinds: **NOT IMPLEMENTED**.

### Recurrence Implementation
Occurrences are **never pre-generated as database rows**.
- Computed on demand in `core/data/quest/QuestOccurrences.kt` via `QuestEntity.occursOn(date)` and `occurrencesOn(date, quests, completions, overrides)`.
- Changes to single occurrences are persisted as rows in `quest_occurrence_overrides`.
- Scope support: `OCCURRENCE` (writes override row), `FUTURE` (splits definition from date in transaction via `QuestRepository.updateFromDate`), `ALL` (updates master definition in `quests`).

### Subtasks, Priority, Effort, Due Date/Time, Linking
- **Subtasks:** NOT IMPLEMENTED (no subtask table or entity column).
- **Priority:** CRITICAL, HIGH, MEDIUM, LOW (`TaskPriority` linear enum). No Eisenhower/urgent-important matrix.
- **Effort:** NOT IMPLEMENTED.
- **Focus Flag:** NOT IMPLEMENTED.
- **Due Date/Time:** `startDate: LocalDate`, `deadline: LocalDate?`, `dueTime: LocalTime?` (all implemented).
- **Linking to Goals:** `goal_id` FK exists in `QuestEntity`, but not exposed in `QuestEditorScreen`.
- **Linking to Habits:** NOT IMPLEMENTED (no habit FK or link table for quests).

### Overdue and Reschedule Behavior
- **Overdue Behavior:** Implemented in `QuestOccurrence.isOverdue(today: LocalDate)`. Evaluates true if `!isCompleted && (quest.deadline ?: quest.startDate) < today`. Only one-off quests become overdue (`effectiveDeadline` is null for recurring quests).
- **Reschedule Counter:** NOT IMPLEMENTED (no reschedule counter column exists).

---

## 6. Shared systems

### Categories / Areas
- **Entity:** `CategoryEntity` (`id: String`, `name: String`, `iconKey: String`, `colorArgb: Int`, `isBuiltIn: Boolean`, `sortOrder: Int`) in `core/data/local/entity/CategoryEntity.kt`.
- **Built-in Categories:** `builtin_work` ("Work"), `builtin_health` ("Health"), `builtin_learning` ("Learning"), `builtin_personal` ("Personal"), `builtin_other` ("Other").
- **References:**
  - `quests.category_id` (FK to `categories.id`, `onDelete = ForeignKey.SET_NULL`).
  - `habits.category_id` (FK to `categories.id`, `onDelete = ForeignKey.SET_NULL`).
  - `goals.category_id` (FK to `categories.id`, `onDelete = ForeignKey.SET_NULL`).
  - `transactions.category` is a legacy plain `String` (not FK).

### Cross-links between Goals, Habits, and Quests
| Link Relationship | Storage / Table | Status |
|---|---|---|
| **Habit → Goal** | `habit_goal_links` (`id`, `habit_id`, `goal_id`, `contribution_rule`) | Table and DAO methods exist; UI linking is NOT IMPLEMENTED |
| **Quest → Goal** | `quests.goal_id` (FK to `goals.id`, `onDelete = ForeignKey.SET_NULL`) | DB FK exists; editor UI selector is NOT IMPLEMENTED |
| **Quest → Milestone** | None | NOT IMPLEMENTED |
| **Quest → Habit** | None | NOT IMPLEMENTED |

### Reminders / Notifications
- **Status:** **NOT IMPLEMENTED**.
- **Permissions:** None declared in `AndroidManifest.xml` (no `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`).
- **Mechanism:** No `AlarmManager`, `WorkManager`, `BroadcastReceiver`, or notification channels exist anywhere in the codebase.

### Dashboard
- Handled by `DashboardViewModel` in `features/dashboard/DashboardViewModel.kt`.
- Data pulled:
  - Selected date from `DateSelectionStateHolder`.
  - Quests: `questRepository.observeOccurrencesOn(date)` and `observeDayLog(date)`.
  - Habits: `habitRepository.observeActiveHabitsWithConfigs()` combined with `observeDailyProgressOn(date)`.
  - Goals: `goalRepository.observeActiveGoalsWithAllConfigs()` combined with `categoryRepository.observeAll()`.
  - Fallback: Uses `DashboardMockData` for preview cards when data is missing.

### Stats
- Handled by `StatisticsScreen.kt` in `features/statistics/StatisticsScreen.kt`.
- **Status:** `FeaturePlaceholder` only. No statistics are queried or displayed from Room.
- `HabitAnalytics.kt` defines calculation methods (`calculateStatistics`, `calculateStreak`), but they are uncalled by any UI screen.

### Backup / Export / Import
- **Status:** **NOT IMPLEMENTED**.
- `android:allowBackup="false"` is set in `AndroidManifest.xml`.
- No export or import logic exists.

### Finance Links to Goals / Habits / Quests
- **Status:** **NOT IMPLEMENTED**.
- `FinanceTransactionEntity` (`core/data/local/entity/FinanceEntities.kt`) has no relation or FK to Goals, Habits, or Quests. `FinanceScreen.kt` is a `FeaturePlaceholder`.

---

## 7. Navigation & screens

### Screen Inventory
| Screen / Destination | Route / ID | File Path | Purpose |
|---|---|---|---|
| **Main Pager** | `"main"` | `VrddhihApp.kt` | Top-level scaffold hosting bottom bar and HorizontalPager |
| **Dashboard** | `TopLevelDestination.DASHBOARD` (`"dashboard"`) | `features/dashboard/DashboardScreen.kt` | Daily overview, date strip, status summary, activity log |
| **Quests** | `TopLevelDestination.QUESTS` (`"quests"`) | `features/quests/QuestsScreen.kt` | List quests, filter (Today/Upcoming/Overdue/Completed), complete quest |
| **Quest Editor** | `"quest_editor?questId={questId}&occurrenceDate={occurrenceDate}"` | `features/quests/QuestEditorScreen.kt` | Full-screen create/edit quest form |
| **Habits** | `TopLevelDestination.HABITS` (`"habits"`) | `features/habits/HabitsScreen.kt` | List active habits, log daily progress, filter by category/frequency |
| **Goals** | `TopLevelDestination.GOALS` (`"goals"`) | `features/goals/GoalsScreen.kt` | List active/completed goals, expand progress, update milestones/counts |
| **Finance** | `TopLevelDestination.FINANCE` (`"finance"`) | `features/finance/FinanceScreen.kt` | Placeholder for future finance tracking |
| **Statistics** | `TopLevelDestination.STATS` (`"statistics"`) | `features/statistics/StatisticsScreen.kt` | Placeholder for future growth analytics |
| **Categories** | `"categories"` | `features/categories/CategoriesScreen.kt` | List, add, edit, and delete custom categories |

### "Create New Item" Flow
There is **NO unified "create item" dialog or picker** (no single button prompting "Quest vs Habit vs Goal").
The creation flow is separated per module:
1. User navigates to the **Quests** tab → clicks bottom-anchored `+ NEW QUEST` button → navigates to `quest_editor` route.
2. User navigates to the **Habits** tab → clicks bottom-anchored `+ NEW HABIT` button → opens `HabitEditorDialog`.
3. User navigates to the **Goals** tab → clicks bottom-anchored `+ NEW GOAL` button → opens `GoalEditorDialog`.

---

## 8. Gap check against this target design

| Feature | Status | File Path / Note |
|---|---|---|
| **Goal progress modes (Milestone / Target-value / Habit-driven)** | PARTIAL | `MILESTONE_BASED` and `NUMERIC_TARGET` exist in `core/data/local/entity/GoalEntities.kt`. Habit-driven mode is **MISSING** (only manual `COMPLETION_BASED` exists). |
| **Goal "why" field, status (Active/Paused/Achieved/Dropped), priority** | PARTIAL | Status exists with values `NOT_STARTED, ACTIVE, PAUSED, COMPLETED, ARCHIVED` in `GoalEntities.kt`. Goal "why" field and goal priority are **MISSING**. |
| **Optional goal target date** | EXISTS | `GoalEntity.targetDate: LocalDate?` in `core/data/local/entity/GoalEntities.kt`. |
| **Habit schedule: times-per-period and every-N-days** | EXISTS | `HabitScheduleType.EVERY_N_DAYS` and `WEEKLY_FREQUENCY` in `core/data/local/entity/HabitEntities.kt` and `HabitScheduler.kt`. |
| **Habit cue field ("After X, I will Y at Z")** | MISSING | Not present in `HabitEntity` in `core/data/local/entity/HabitEntities.kt`. |
| **Habit minimum/"tiny" version and MINIMUM log status** | MISSING | Not present in `HabitEntity` or `HabitCompletionState` in `core/data/local/entity/HabitEntities.kt`. |
| **Habit strength score (soft misses) in addition to streaks** | MISSING | `HabitAnalytics.kt` computes simple streak and completion rate; strength decay / soft misses score is not implemented. |
| **Habit skip/pause** | PARTIAL | Skip date is supported via `HabitExcludedDate` entity and `HabitCompletionState.SKIPPED`, but habit pause flag is **MISSING** and no UI control exists to skip. |
| **Routine grouping (morning/evening)** | MISSING | Not present in `HabitEntity` or UI. |
| **Quest kinds (Main/Side/Recurring)** | PARTIAL | Recurrence is supported via `QuestRecurrence`, but "Main" and "Side" quest kinds are **MISSING**. |
| **Quest subtasks, priority (urgent x important), effort, focus flag** | PARTIAL | `TaskPriority` exists as a linear enum (`CRITICAL, HIGH, MEDIUM, LOW`). Subtasks, Eisenhower (urgent x important) matrix, effort, and focus flag are **MISSING**. |
| **Quest reschedule counter** | MISSING | Not present in `QuestEntity` or `QuestOccurrenceOverrideEntity`. |
| **Habit/Quest link to a goal (and quest to milestone)** | PARTIAL | `quests.goal_id` and `habit_goal_links` table exist in Room. UI linkage and quest-to-milestone links are **MISSING**. |
| **Shared life areas used by all three modules** | EXISTS | `CategoryEntity` in `core/data/local/entity/CategoryEntity.kt` is referenced by `quests.category_id`, `habits.category_id`, and `goals.category_id`. |
| **Single unified create action / chooser** | MISSING | Creation actions are separate buttons on each tab (`+ NEW QUEST`, `+ NEW HABIT`, `+ NEW GOAL`). There is no unified create sheet. |
