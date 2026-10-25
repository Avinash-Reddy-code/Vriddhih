package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.FinanceDao
import com.vrddhih.app.core.data.local.dao.HabitDao
import com.vrddhih.app.core.data.local.dao.QuestDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Counts of real records logged on one calendar day. */
data class DayActivity(
    val questCompletions: Int,
    val habitCheckIns: Int,
    val transactions: Int,
) {
    val isEmpty: Boolean get() = questCompletions == 0 && habitCheckIns == 0 && transactions == 0
}

/** Date-scoped read model shared by screens that summarise a selected day. */
class DayActivityRepository(
    private val questDao: QuestDao,
    private val habitDao: HabitDao,
    private val financeDao: FinanceDao,
) {
    fun observeDayActivity(date: LocalDate): Flow<DayActivity> = combine(
        questDao.observeCompletionCountOn(date),
        habitDao.observeCheckInCountOn(date),
        financeDao.observeTransactionCountOn(date),
    ) { quests, habits, transactions -> DayActivity(quests, habits, transactions) }
}
