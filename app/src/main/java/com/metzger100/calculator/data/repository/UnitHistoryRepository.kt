// com.metzger100.calculator.data.repository.UnitHistoryRepository.kt
package com.metzger100.calculator.data.repository

import com.metzger100.calculator.data.local.dao.UnitHistoryDao
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity
import javax.inject.Inject

class UnitHistoryRepository @Inject constructor(
    private val dao: UnitHistoryDao
) {
    private val MaxPerCategory = 25

    suspend fun insert(
        category: String,
        fromValue: String,
        fromUnit: String,
        toValue: String,
        toUnit: String,
        timestamp: Long
    ) {
        val e = UnitHistoryEntity(
            category = category,
            fromValue = fromValue,
            fromUnit = fromUnit,
            toValue = toValue,
            toUnit = toUnit,
            timestamp = timestamp
        )
        dao.insertAndTrim(e, MaxPerCategory)
    }

    suspend fun getHistory(category: String): List<UnitHistoryEntity> =
        dao.getAllByCategory(category)

    suspend fun clearHistory(category: String) =
        dao.clearAllByCategory(category)
}