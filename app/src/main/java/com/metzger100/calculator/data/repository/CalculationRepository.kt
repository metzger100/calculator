package com.metzger100.calculator.data.repository

import com.metzger100.calculator.data.local.dao.CalculationDao
import com.metzger100.calculator.data.local.entity.CalculationEntity
import javax.inject.Inject

class CalculationRepository @Inject constructor(
    private val calcdao: CalculationDao
) {

    suspend fun insert(input: String, result: String) {
        val entity = CalculationEntity(input = input, result = result)
        calcdao.insertAndTrim(entity, maxSize = 25)
    }

    suspend fun getHistory(): List<CalculationEntity> {
        return calcdao.getAll()
    }

    suspend fun clearHistory() {
        calcdao.clearAll()
    }
}
