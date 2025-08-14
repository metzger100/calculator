// com.metzger100.calculator.data.local.entity.UnitHistoryEntity.kt
package com.metzger100.calculator.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit_history",
    indices = [Index("category")] // fast per-category queries & trims
)
data class UnitHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,     // e.g. "Length", "Temperature", "Data" (route segment)
    val fromValue: String,
    val fromUnit: String,
    val toValue: String,
    val toUnit: String,
    val timestamp: Long       // epoch millis
)