package com.metzger100.calculator.data.repository

import com.metzger100.calculator.data.local.dao.CurrencyHistoryDao
import com.metzger100.calculator.data.local.entity.CurrencyHistoryEntity
import javax.inject.Inject

class CurrencyHistoryRepository @Inject constructor(private val dao: CurrencyHistoryDao) {

    suspend fun insert(amountFrom: String, currencyFrom: String, amountTo: String, currencyTo: String, timestamp: Long) {
        val entity = CurrencyHistoryEntity(
            amountFrom = amountFrom,
            currencyFrom = currencyFrom,
            amountTo = amountTo,
            currencyTo = currencyTo,
            timestamp = timestamp
        )
        dao.insertAndTrim(entity, maxSize = 25)
    }

    suspend fun getHistory(): List<CurrencyHistoryEntity> = dao.getAll()

    suspend fun clearHistory() = dao.clearAll()
}