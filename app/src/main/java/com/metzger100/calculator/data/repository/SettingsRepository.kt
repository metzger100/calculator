package com.metzger100.calculator.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.metzger100.calculator.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val THEME_MODE = stringPreferencesKey("theme_mode")

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { pref ->
        when (pref[THEME_MODE]) {
            ThemeMode.LIGHT.name  -> ThemeMode.LIGHT
            ThemeMode.DARK.name   -> ThemeMode.DARK
            else                  -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }
}
