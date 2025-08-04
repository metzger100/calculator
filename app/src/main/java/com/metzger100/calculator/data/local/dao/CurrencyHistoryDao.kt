package com.metzger100.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.metzger100.calculator.data.local.entity.CurrencyHistoryEntity

@Dao
interface CurrencyHistoryDao {

    @Transaction
    suspend fun insertAndTrim(entity: CurrencyHistoryEntity, maxSize: Int) {
        insert(entity)
        val count = getCount()
        if (count > maxSize) {
            deleteOldestEntries(count - maxSize)
        }
    }

    @Insert
    suspend fun insert(entity: CurrencyHistoryEntity)

    @Query("SELECT COUNT(*) FROM currency_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM currency_history WHERE id IN (SELECT id FROM currency_history ORDER BY id ASC LIMIT :count)")
    suspend fun deleteOldestEntries(count: Int)

    @Query("SELECT * FROM currency_history ORDER BY id DESC") // newest first
    suspend fun getAll(): List<CurrencyHistoryEntity>

    @Query("DELETE FROM currency_history")
    suspend fun clearAll()
}