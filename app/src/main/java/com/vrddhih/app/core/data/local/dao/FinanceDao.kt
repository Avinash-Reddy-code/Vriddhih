package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.FinanceTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Income and expense totals in minor units for one currency. */
data class FinanceTotals(
    val incomeMinor: Long,
    val expenseMinor: Long,
) {
    val netMinor: Long get() = incomeMinor - expenseMinor
}

@Dao
interface FinanceDao {
    @Insert
    suspend fun insertTransaction(transaction: FinanceTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: FinanceTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): FinanceTransactionEntity?

    @Query("SELECT * FROM transactions WHERE transaction_date = :date ORDER BY created_at, id")
    fun observeTransactionsOn(date: LocalDate): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transaction_date BETWEEN :start AND :end ORDER BY transaction_date, created_at, id")
    fun observeTransactionsBetween(start: LocalDate, end: LocalDate): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE transaction_date = :date")
    fun observeTransactionCountOn(date: LocalDate): Flow<Int>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount_minor END), 0) AS incomeMinor,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount_minor END), 0) AS expenseMinor
        FROM transactions
        WHERE currency_code = :currencyCode AND transaction_date BETWEEN :start AND :end
        """,
    )
    fun observeTotals(currencyCode: String, start: LocalDate, end: LocalDate): Flow<FinanceTotals>
}
