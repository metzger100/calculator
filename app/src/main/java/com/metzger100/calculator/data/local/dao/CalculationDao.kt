package com.metzger100.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.metzger100.calculator.data.local.entity.CalculationEntity

@Dao
interface CalculationDao {

    @Transaction
    suspend fun insertAndTrim(entity: CalculationEntity, maxSize: Int) {
        insert(entity)
        val count = getCount()
        if (count > maxSize) {
            deleteOldestEntries(count - maxSize)
        }
    }

    @Insert
    suspend fun insert(calculation: CalculationEntity)

    @Query("SELECT COUNT(*) FROM calculations")
    suspend fun getCount(): Int

    @Query(
        "DELETE FROM calculations " +
                "WHERE id IN (" +
                "SELECT id FROM calculations ORDER BY id ASC LIMIT :count" +
                ")"
    )
    suspend fun deleteOldestEntries(count: Int)

    @Query("SELECT * FROM calculations ORDER BY id ASC")
    suspend fun getAll(): List<CalculationEntity>

    @Query("DELETE FROM calculations")
    suspend fun clearAll()
}
