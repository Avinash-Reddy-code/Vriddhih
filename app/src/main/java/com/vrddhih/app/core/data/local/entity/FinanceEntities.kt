package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

/**
 * An income or expense record. [amountMinor] is a positive amount in the currency's minor unit
 * (for INR, paise: ₹12.50 = 1250). The direction comes from [type]. Floating point is never used for money.
 */
@Entity(tableName = "transactions", indices = [Index("transaction_date"), Index("category")])
data class FinanceTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code", defaultValue = "INR") val currencyCode: String = "INR",
    @ColumnInfo(defaultValue = "") val category: String = "",
    @ColumnInfo(defaultValue = "") val note: String = "",
    /** The local calendar day the transaction happened. */
    @ColumnInfo(name = "transaction_date") val date: LocalDate,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
