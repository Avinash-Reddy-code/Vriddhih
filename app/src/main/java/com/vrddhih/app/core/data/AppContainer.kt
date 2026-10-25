package com.vrddhih.app.core.data

import android.content.Context
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.repository.DayActivityRepository
import com.vrddhih.app.core.data.repository.FinanceRepository
import com.vrddhih.app.core.data.repository.GoalRepository
import com.vrddhih.app.core.data.repository.HabitRepository
import com.vrddhih.app.core.data.repository.ProgressionRepository
import com.vrddhih.app.core.data.repository.QuestRepository
import java.time.Clock

/**
 * Manual dependency wiring for the app process. The database opens lazily the first time a
 * repository is used.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val clock: Clock = Clock.systemUTC()

    val database: VrddhihDatabase by lazy { VrddhihDatabase.build(appContext) }

    val questRepository by lazy { QuestRepository(database.questDao(), clock) }
    val habitRepository by lazy { HabitRepository(database.habitDao(), clock) }
    val goalRepository by lazy { GoalRepository(database.goalDao(), clock) }
    val financeRepository by lazy { FinanceRepository(database.financeDao(), clock) }
    val progressionRepository by lazy { ProgressionRepository(database.xpEventDao(), clock) }
    val dayActivityRepository by lazy {
        DayActivityRepository(database.questDao(), database.habitDao(), database.financeDao())
    }
}
