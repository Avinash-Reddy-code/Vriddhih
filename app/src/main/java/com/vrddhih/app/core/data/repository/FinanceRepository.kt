package com.vrddhih.app.core.data.repository

import com.vrddhih.app.core.data.local.dao.FinanceDao
import com.vrddhih.app.core.data.local.dao.FinanceTotals
import com.vrddhih.app.core.data.local.entity.FinanceTransactionEntity
import com.vrddhih.app.core.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class FinanceRepository(
    private val dao: FinanceDao,
    private val clock: Clock,
) {
    fun observeTransactionsOn(date: LocalDate): Flow<List<FinanceTransactionEntity>> = dao.observeTransactionsOn(date)

    fun observeMonthTotals(month: YearMonth, currencyCode: String = DEFAULT_CURRENCY): Flow<FinanceTotals> =
        dao.observeTotals(currencyCode, month.atDay(1), month.atEndOfMonth())

    /** [amountMinor] is a positive amount in minor units (paise for INR). */
    suspend fun addTransaction(
        type: TransactionType,
        amountMinor: Long,
        date: LocalDate,
        category: String = "",
        note: String = "",
        currencyCode: String = DEFAULT_CURRENCY,
    ): Long {
        require(amountMinor > 0) { "Amount must be positive; use the transaction type for direction" }
        val now = Instant.now(clock)
        return dao.insertTransaction(
            FinanceTransactionEntity(
                type = type,
                amountMinor = amountMinor,
                currencyCode = currencyCode,
                category = category,
                note = note,
                date = date,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateTransaction(transaction: FinanceTransactionEntity) {
        require(transaction.amountMinor > 0) { "Amount must be positive" }
        dao.updateTransaction(transaction.copy(updatedAt = Instant.now(clock)))
    }

    suspend fun deleteTransaction(transaction: FinanceTransactionEntity) = dao.deleteTransaction(transaction)

    companion object {
        const val DEFAULT_CURRENCY = "INR"
    }
}
