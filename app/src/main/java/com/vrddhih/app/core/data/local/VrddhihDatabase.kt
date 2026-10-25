package com.vrddhih.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.vrddhih.app.core.data.local.dao.FinanceDao
import com.vrddhih.app.core.data.local.dao.GoalDao
import com.vrddhih.app.core.data.local.dao.HabitDao
import com.vrddhih.app.core.data.local.dao.QuestDao
import com.vrddhih.app.core.data.local.dao.XpEventDao
import com.vrddhih.app.core.data.local.entity.FinanceTransactionEntity
import com.vrddhih.app.core.data.local.entity.GoalEntity
import com.vrddhih.app.core.data.local.entity.HabitCheckInEntity
import com.vrddhih.app.core.data.local.entity.HabitEntity
import com.vrddhih.app.core.data.local.entity.MilestoneEntity
import com.vrddhih.app.core.data.local.entity.QuestCompletionEntity
import com.vrddhih.app.core.data.local.entity.QuestEntity
import com.vrddhih.app.core.data.local.entity.XpEventEntity

/** The app's single on-device database, stored in app-private storage. */
@Database(
    entities = [
        QuestEntity::class,
        QuestCompletionEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        FinanceTransactionEntity::class,
        XpEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VrddhihDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun financeDao(): FinanceDao
    abstract fun xpEventDao(): XpEventDao

    companion object {
        const val NAME = "vrddhih.db"

        /**
         * Destructive migration fallback is intentionally not enabled. A missing migration fails loudly
         * instead of silently erasing personal data.
         */
        fun build(context: Context): VrddhihDatabase =
            Room.databaseBuilder(context.applicationContext, VrddhihDatabase::class.java, NAME)
                .addMigrations(*VrddhihMigrations.ALL)
                .build()
    }
}

/**
 * Explicit schema migrations. To change the schema: bump [Database.version], add a [Migration] here,
 * commit the new exported schema in `app/schemas`, and add a migration test.
 */
object VrddhihMigrations {
    val ALL: Array<Migration> = emptyArray()
}
