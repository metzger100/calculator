package com.metzger100.calculator.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.metzger100.calculator.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val THEME_MODE = stringPreferencesKey("theme_mode")
private val OPEN_KEYBOARD_ON_START = booleanPreferencesKey("open_keyboard_on_start")
private val SCIENTIFIC_ON_START = booleanPreferencesKey("scientific_on_start")

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

    val openKeyboardOnStartFlow: Flow<Boolean> =
        dataStore.data.map { it[OPEN_KEYBOARD_ON_START] ?: false }

    val scientificOnStartFlow: Flow<Boolean> =
        dataStore.data.map { it[SCIENTIFIC_ON_START] ?: false }

    // For settings as one state, combine all (for initial app load!)
    data class AppSettings(
        val themeMode: ThemeMode,
        val openKeyboardOnStart: Boolean,
        val scientificOnStart: Boolean
    )

    val appSettingsFlow: Flow<AppSettings> = combine(
        themeModeFlow, openKeyboardOnStartFlow, scientificOnStartFlow
    ) { mode, openKbd, sci ->
        AppSettings(mode, openKbd, sci)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }
    suspend fun setOpenKeyboardOnStart(value: Boolean) {
        dataStore.edit { it[OPEN_KEYBOARD_ON_START] = value }
    }
    suspend fun setScientificOnStart(value: Boolean) {
        dataStore.edit { it[SCIENTIFIC_ON_START] = value }
    }
}
