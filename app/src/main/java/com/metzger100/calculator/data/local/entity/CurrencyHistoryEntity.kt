package com.metzger100.calculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_history")
data class CurrencyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountFrom: String,
    val currencyFrom: String,
    val amountTo: String,
    val currencyTo: String,
    val timestamp: Long
)