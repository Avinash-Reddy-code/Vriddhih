package com.vrddhih.app.core.data.habit

import com.vrddhih.app.core.data.local.entity.HabitScheduleType
import com.vrddhih.app.core.data.local.entity.HabitType
import com.vrddhih.app.core.data.local.entity.NumericDirection
import java.time.DayOfWeek
import java.time.LocalDate

enum class HabitDraftError {
    NAME_REQUIRED,
    CATEGORY_REQUIRED,
    NO_WEEKDAYS,
    NO_MONTH_DAYS,
    CHECKLIST_EMPTY,
}

data class HabitExtraGoalDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val targetValue: Long = 1,
    val durationHours: Int = 0,
    val durationMinutes: Int = 15,
    val condition: NumericDirection = NumericDirection.AT_LEAST,
)

data class HabitDraft(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val type: HabitType = HabitType.YES_NO,
    // Frequency
    val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    val intervalText: String = "1",
    val weekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val monthDays: Set<Int> = setOf(1), // 1..31
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val xpPerCompletion: Int = 5,
    // Numeric config
    val numericTarget: String = "1",
    val numericUnit: String = "",
    val numericCondition: NumericDirection = NumericDirection.AT_LEAST,
    val numericIncrement: String = "1",
    // Timer config
    val timerHours: String = "0",
    val timerMinutes: String = "15",
    val timerCondition: NumericDirection = NumericDirection.AT_LEAST,
    // Checklist config
    val checklistItems: List<String> = listOf(""),
    val checklistTargetCount: Int? = null,
    // Extra Goals (for Numeric, Timer, Checklist)
    val extraGoals: List<HabitExtraGoalDraft> = emptyList(),
) {
    val parsedInterval: Int?
        get() = intervalText.toIntOrNull()?.takeIf { it in 1..MAX_INTERVAL }

    val parsedNumericTarget: Long
        get() = numericTarget.toLongOrNull()?.coerceAtLeast(1L) ?: 1L

    val parsedNumericIncrement: Long
        get() = numericIncrement.toLongOrNull()?.coerceAtLeast(1L) ?: 1L

    val parsedTimerTargetMinutes: Int
        get() {
            val h = timerHours.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val m = timerMinutes.toIntOrNull()?.coerceIn(0, 59) ?: 15
            val total = h * 60 + m
            return maxOf(1, total)
        }

    fun validate(): Set<HabitDraftError> {
        val errors = mutableSetOf<HabitDraftError>()
        if (name.isBlank()) {
            errors.add(HabitDraftError.NAME_REQUIRED)
        }
        if (categoryId.isNullOrBlank()) {
            errors.add(HabitDraftError.CATEGORY_REQUIRED)
        }
        if (scheduleType == HabitScheduleType.SELECTED_WEEKDAYS && weekdays.isEmpty()) {
            errors.add(HabitDraftError.NO_WEEKDAYS)
        }
        if (scheduleType == HabitScheduleType.MONTHLY_DAYS && monthDays.isEmpty()) {
            errors.add(HabitDraftError.NO_MONTH_DAYS)
        }
        if (type == HabitType.CHECKLIST && checklistItems.none { it.isNotBlank() }) {
            errors.add(HabitDraftError.CHECKLIST_EMPTY)
        }
        return errors
    }

    companion object {
        const val MAX_INTERVAL = 365
    }
}

object HabitExtraGoalsHelper {
    private const val PREFIX = "<!-- EXTRA_GOALS:"
    private const val SUFFIX = "-->"

    fun parse(description: String): Pair<String, List<HabitExtraGoalDraft>> {
        val startIndex = description.indexOf(PREFIX)
        if (startIndex == -1) return description.trim() to emptyList()
        val endIndex = description.indexOf(SUFFIX, startIndex)
        if (endIndex == -1) return description.trim() to emptyList()

        val cleanNotes = (description.substring(0, startIndex) + description.substring(endIndex + SUFFIX.length)).trim()
        val payload = description.substring(startIndex + PREFIX.length, endIndex).trim()

        val goals = payload.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 4) {
                val duration = parts[2].toIntOrNull() ?: 0
                HabitExtraGoalDraft(
                    name = parts[0],
                    targetValue = parts[1].toLongOrNull() ?: 1L,
                    durationHours = duration / 60,
                    durationMinutes = duration % 60,
                    condition = runCatching { NumericDirection.valueOf(parts[3]) }.getOrDefault(NumericDirection.AT_LEAST),
                )
            } else null
        }
        return cleanNotes to goals
    }

    fun format(userNotes: String, extraGoals: List<HabitExtraGoalDraft>): String {
        val trimmedNotes = userNotes.trim()
        val validGoals = extraGoals.filter { it.name.isNotBlank() }
        if (validGoals.isEmpty()) return trimmedNotes

        val serialized = validGoals.joinToString(";;") { goal ->
            val totalMinutes = goal.durationHours * 60 + goal.durationMinutes
            "${goal.name.trim()}|${goal.targetValue}|$totalMinutes|${goal.condition.name}"
        }
        return if (trimmedNotes.isEmpty()) {
            "$PREFIX$serialized$SUFFIX"
        } else {
            "$trimmedNotes\n\n$PREFIX$serialized$SUFFIX"
        }
    }
}
