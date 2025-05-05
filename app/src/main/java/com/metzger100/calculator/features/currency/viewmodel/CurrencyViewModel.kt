package com.metzger100.calculator.features.currency.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metzger100.calculator.data.ConnectivityObserver
import com.metzger100.calculator.data.local.entity.CurrencyPrefsEntity
import com.metzger100.calculator.data.repository.CurrencyRepository
import com.metzger100.calculator.util.format.NumberFormatService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

// 1) Datenklasse für den gesamten UI‑State
data class CurrencyUiState(
    val currency1: String = "USD",
    val currency2: String = "EUR",
    val selectedField: Int = 1,
    val value1: String = "",
    val value2: String = ""
)

@HiltViewModel
@SuppressLint("DefaultLocale")
class CurrencyViewModel @Inject constructor(
    private val repo: CurrencyRepository,
    private val numberFormatService: NumberFormatService,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    companion object {
        private const val TAG = "CurrencyViewModel"
    }

    // monolithischer UI‑State
    var uiState by mutableStateOf(CurrencyUiState())
        private set

    // 2) Basis-Währung als Flow
    private val _base = MutableStateFlow("USD")
    val base: StateFlow<String> = _base

    // 4) Refresh-Trigger: bei jeder Erhöhung dieser Zahl feuern wir die Flows neu ab
    private val refreshTrigger = MutableStateFlow(0)

    // 5) currenciesWithTitles reagiert auf refreshTrigger
    @OptIn(ExperimentalCoroutinesApi::class)
    val currenciesWithTitles: StateFlow<List<Pair<String, String>>> =
        refreshTrigger
            .flatMapLatest {
                val currentOnlineStatus = connectivityObserver.isOnline()
                repo.getCurrenciesFlow(isOnline = currentOnlineStatus)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    // 6) rates reagiert auf refreshTrigger und auf Basis-Wechsel
    @OptIn(ExperimentalCoroutinesApi::class)
    val rates: StateFlow<Map<String, Double>> =
        combine(refreshTrigger, base) { _, b -> b }
            .flatMapLatest { b ->
                val currentOnlineStatus = connectivityObserver.isOnline()
                repo.getRatesFlow(base = b, isOnline = currentOnlineStatus)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyMap()
            )

    // 7) Datum der letzten API-Antwort
    private val _lastApiDate = MutableStateFlow<LocalDate?>(null)
    val lastApiDate: StateFlow<LocalDate?> = _lastApiDate

    init {
        viewModelScope.launch {
            Log.d(TAG, "init: START")
            // Lade gespeicherte Prefs
            repo.getPrefs()?.let { prefs ->
                uiState = CurrencyUiState(
                    currency1 = prefs.currency1,
                    currency2 = prefs.currency2,
                    selectedField = prefs.activeField,
                    value1 = prefs.amount1,
                    value2 = prefs.amount2
                )
                _base.value = prefs.currency1
                Log.d(TAG, "init: loaded prefs → $uiState")
            }
            // Erste Initial-Abfrage
            refreshTrigger.update { it + 1 }
        }

        // 9) Auf jeden neuen Satz von Kursen reagieren:
        viewModelScope.launch {
            rates.collect { rateMap ->
                _lastApiDate.value = repo.getLastApiDateForBase(base.value)
                recalc()
            }
        }
    }

    /** Manuelles Refresh feuert die beiden Flows neu ab */
    fun refreshData() {
        refreshTrigger.update { it + 1 }
    }

    fun formatNumber(input: String, shortMode: Boolean): String =
        numberFormatService.formatNumber(input, shortMode, inputLine = false)

    private var prefsJob: Job? = null

    private suspend fun savePrefs(state: CurrencyUiState) {
        repo.savePrefs(
            CurrencyPrefsEntity(
                id          = 1,
                activeField = state.selectedField,
                currency1   = state.currency1,
                currency2   = state.currency2,
                amount1     = state.value1,
                amount2     = state.value2
            )
        )
    }

    fun onSelectField(field: Int) {
        uiState = uiState.copy(selectedField = field)
        prefsJob?.cancel()
        prefsJob = viewModelScope.launch {
            delay(500)
            savePrefs(uiState)
        }
    }

    fun onCurrencyChanged1(code: String) {
        uiState = uiState.copy(currency1 = code)
        _base.value = code
        recalc()
        prefsJob?.cancel()
        prefsJob = viewModelScope.launch {
            delay(500)
            savePrefs(uiState)
        }
    }

    fun onCurrencyChanged2(code: String) {
        uiState = uiState.copy(currency2 = code)
        recalc()
        prefsJob?.cancel()
        prefsJob = viewModelScope.launch {
            delay(500)
            savePrefs(uiState)
        }
    }

    fun onValueChange(newValue: String) {
        uiState = if (uiState.selectedField == 1) {
            uiState.copy(value1 = newValue)
        } else {
            uiState.copy(value2 = newValue)
        }
        recalc()
        prefsJob?.cancel()
        prefsJob = viewModelScope.launch {
            delay(500)
            savePrefs(uiState)
        }
    }

    private fun recalc() {
        // Eingabe und Codes ermitteln
        val (fromAmt, fromCode, toCode) = if (uiState.selectedField == 1) {
            Triple(uiState.value1, uiState.currency1, uiState.currency2)
        } else {
            Triple(uiState.value2, uiState.currency2, uiState.currency1)
        }

        // Bei leerer Eingabe das Gegenfeld zurücksetzen
        if (fromAmt.isBlank()) {
            uiState = if (uiState.selectedField == 1) {
                uiState.copy(value2 = "")
            } else {
                uiState.copy(value1 = "")
            }
            return
        }

        // Konvertierung durchführen
        val result = convert(fromAmt, fromCode, toCode, rates.value)
        uiState = if (uiState.selectedField == 1) {
            uiState.copy(value2 = result)
        } else {
            uiState.copy(value1 = result)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convert(
        amount: String,
        from: String,
        to: String,
        rates: Map<String, Double>
    ): String {
        val normalized = amount.replace(',', '.')
        val aBD = runCatching { BigDecimal(normalized) }.getOrDefault(BigDecimal.ZERO)
        val fromRate = rates[from.lowercase()]?.let(BigDecimal::valueOf) ?: return "0"
        val toRate   = rates[to.lowercase()]?.let(BigDecimal::valueOf)   ?: return "0"
        val mc = MathContext(16, RoundingMode.HALF_UP)
        return runCatching {
            aBD.divide(fromRate, mc)
                .multiply(toRate, mc)
                .stripTrailingZeros()
                .toPlainString()
        }.getOrDefault("0")
    }
}