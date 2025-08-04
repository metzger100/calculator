package com.metzger100.calculator.di.module

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.metzger100.calculator.data.ConnectivityObserver
import com.metzger100.calculator.data.repository.CalculationRepository
import com.metzger100.calculator.data.local.database.CalculatorDatabase
import com.metzger100.calculator.data.local.dao.CalculationDao
import com.metzger100.calculator.data.local.dao.CurrencyHistoryDao
import com.metzger100.calculator.data.local.dao.CurrencyListDao
import com.metzger100.calculator.data.local.dao.CurrencyPrefsDao
import com.metzger100.calculator.data.local.dao.CurrencyRateDao
import com.metzger100.calculator.data.local.database.MIGRATION_1_2
import com.metzger100.calculator.data.local.database.MIGRATION_2_3
import com.metzger100.calculator.data.repository.SettingsRepository
import com.metzger100.calculator.di.IoDispatcher
import com.metzger100.calculator.di.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): CalculatorDatabase {
        return Room.databaseBuilder(
            app,
            CalculatorDatabase::class.java,
            "calculator.db"
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideCalculationDao(db: CalculatorDatabase): CalculationDao {
        return db.calculationDao()
    }

    @Provides
    fun provideRepository(dao: CalculationDao): CalculationRepository {
        return CalculationRepository(dao)
    }

    @Provides
    fun provideCurrencyDao(db: CalculatorDatabase): CurrencyRateDao =
        db.currencyRateDao()

    @Provides
    fun provideCurrencyListDao(db: CalculatorDatabase): CurrencyListDao =
        db.currencyListDao()

    @Provides
    fun provideCurrencyHistoryDao(db: CalculatorDatabase): CurrencyHistoryDao =
        db.currencyHistoryDao()

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(CIO)

    @Provides
    fun provideCurrencyPrefsDao(db: CalculatorDatabase): CurrencyPrefsDao =
        db.currencyPrefsDao()

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver = ConnectivityObserver(context)

    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(
        @ApplicationContext ctx: Context
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { ctx.preferencesDataStoreFile("user_settings") }
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @SettingsDataStore ds: DataStore<Preferences>
    ) = SettingsRepository(ds)
}