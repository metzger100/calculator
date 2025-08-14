package com.metzger100.calculator.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.metzger100.calculator.data.local.dao.CalculationDao
import com.metzger100.calculator.data.local.dao.CurrencyHistoryDao
import com.metzger100.calculator.data.local.dao.CurrencyListDao
import com.metzger100.calculator.data.local.dao.CurrencyPrefsDao
import com.metzger100.calculator.data.local.dao.CurrencyRateDao
import com.metzger100.calculator.data.local.dao.UnitHistoryDao
import com.metzger100.calculator.data.local.entity.CalculationEntity
import com.metzger100.calculator.data.local.entity.CurrencyHistoryEntity
import com.metzger100.calculator.data.local.entity.CurrencyListEntity
import com.metzger100.calculator.data.local.entity.CurrencyPrefsEntity
import com.metzger100.calculator.data.local.entity.CurrencyRateEntity
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity

@Database(
    entities = [
        CalculationEntity::class,
        CurrencyRateEntity::class,
        CurrencyListEntity::class,
        CurrencyHistoryEntity::class,
        CurrencyPrefsEntity::class,
        UnitHistoryEntity::class,
    ],
    version = 4
)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun currencyRateDao(): CurrencyRateDao

    abstract fun currencyHistoryDao(): CurrencyHistoryDao
    abstract fun currencyListDao(): CurrencyListDao
    abstract fun currencyPrefsDao(): CurrencyPrefsDao
    abstract fun unitHistoryDao(): UnitHistoryDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) neue Tabelle anlegen
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `currency_prefs` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `activeField` INTEGER NOT NULL,
                `currency1` TEXT NOT NULL,
                `currency2` TEXT NOT NULL,
                `amount1` TEXT NOT NULL,
                `amount2` TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 2) optional: einen Default-Datensatz einfügen,
        //    damit get() nicht null zurückgibt
        db.execSQL(
            """
            INSERT OR IGNORE INTO `currency_prefs`
                (id, activeField, currency1, currency2, amount1, amount2)
            VALUES
                (1, 1, 'USD', 'EUR', '1', '0')
            """
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `currency_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `amountFrom` TEXT NOT NULL,
                `currencyFrom` TEXT NOT NULL,
                `amountTo` TEXT NOT NULL,
                `currencyTo` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `unit_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `category` TEXT NOT NULL,
                `fromValue` TEXT NOT NULL,
                `fromUnit` TEXT NOT NULL,
                `toValue` TEXT NOT NULL,
                `toUnit` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_unit_history_category` ON `unit_history`(`category`)"
        )
    }
}