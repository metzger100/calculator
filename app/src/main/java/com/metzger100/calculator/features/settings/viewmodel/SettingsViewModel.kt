package com.metzger100.calculator.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metzger100.calculator.data.repository.SettingsRepository
import com.metzger100.calculator.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        repo.themeModeFlow.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ThemeMode.SYSTEM
        )

    val openKeyboardOnStart: StateFlow<Boolean> =
        repo.openKeyboardOnStartFlow.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), false)

    val scientificOnStart: StateFlow<Boolean> =
        repo.scientificOnStartFlow.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), false)

    // For initial app-wide usage
    val appSettings: StateFlow<SettingsRepository.AppSettings?> =
        repo.appSettingsFlow.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), null
        )

    fun onThemeSelected(mode: ThemeMode) = viewModelScope.launch {
        repo.setThemeMode(mode)
    }
    fun onOpenKeyboardOnStartChange(value: Boolean) = viewModelScope.launch {
        repo.setOpenKeyboardOnStart(value)
    }
    fun onScientificOnStartChange(value: Boolean) = viewModelScope.launch {
        repo.setScientificOnStart(value)
    }

}