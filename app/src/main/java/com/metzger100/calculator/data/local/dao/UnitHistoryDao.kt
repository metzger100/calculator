// com.metzger100.calculator.data.local.dao.UnitHistoryDao.kt
package com.metzger100.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity

@Dao
interface UnitHistoryDao {

    @Transaction
    suspend fun insertAndTrim(entity: UnitHistoryEntity, maxPerCategory: Int) {
        insert(entity)
        val count = getCountByCategory(entity.category)
        if (count > maxPerCategory) {
            deleteOldestInCategory(entity.category, count - maxPerCategory)
        }
    }

    @Insert
    suspend fun insert(entity: UnitHistoryEntity)

    @Query("SELECT COUNT(*) FROM unit_history WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int

    @Query("""
        DELETE FROM unit_history
        WHERE id IN (
            SELECT id FROM unit_history
            WHERE category = :category
            ORDER BY id ASC
            LIMIT :count
        )
    """)
    suspend fun deleteOldestInCategory(category: String, count: Int)

    @Query("SELECT * FROM unit_history WHERE category = :category ORDER BY id DESC")
    suspend fun getAllByCategory(category: String): List<UnitHistoryEntity>

    @Query("DELETE FROM unit_history WHERE category = :category")
    suspend fun clearAllByCategory(category: String)
}