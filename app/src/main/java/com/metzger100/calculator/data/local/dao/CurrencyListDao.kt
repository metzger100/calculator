// com/metzger100/calculator/data/local/CurrencyListDao.kt
package com.metzger100.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.metzger100.calculator.data.local.entity.CurrencyListEntity

@Dao
interface CurrencyListDao {
    @Query("SELECT * FROM currency_list WHERE id = 0")
    suspend fun get(): CurrencyListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CurrencyListEntity)
}
