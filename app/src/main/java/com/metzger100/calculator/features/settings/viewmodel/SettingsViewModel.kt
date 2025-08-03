package com.metzger100.calculator.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metzger100.calculator.data.repository.SettingsRepository
import com.metzger100.calculator.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
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
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            ThemeMode.SYSTEM
        )

    fun onThemeSelected(mode: ThemeMode) = viewModelScope.launch {
        repo.setThemeMode(mode)
    }
}