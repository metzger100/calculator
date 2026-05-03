package com.metzger100.calculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey val base: String,
    val json: String,           // rohes JSON aus der API
    val timestamp: Long         // epochMillis, wann geholt
)
