// com.metzger100.calculator.features.unit.viewmodel.UnitConverterViewModel.kt
package com.metzger100.calculator.features.unit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity
import com.metzger100.calculator.data.repository.UnitHistoryRepository
import com.metzger100.calculator.features.unit.ui.UnitConverterConstants
import com.metzger100.calculator.features.unit.ui.UnitConverterConstants.UnitDef
import com.metzger100.calculator.util.format.NumberFormatService
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

// 1. Monolithischer UI-State
data class UnitConverterUiState(
    val selectedField: Int = 1,
    val fromUnit: UnitDef,
    val toUnit: UnitDef,
    val fromValue: String = "",
    val toValue: String = ""
)

@HiltViewModel
class UnitConverterViewModel @Inject constructor(
    private val numberFormatService: NumberFormatService,
    private val unitHistoryRepo: UnitHistoryRepository,               // ← NEW
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Kategorie aus dem SavedStateHandle
    private val category: String = savedStateHandle.get<String>("category")
        ?: UnitConverterConstants.units.keys.first()

    /** Alle UnitDef dieser Kategorie */
    val availableUnits: List<UnitDef> =
        UnitConverterConstants.units[category] ?: emptyList()

    // 2. Single source of truth für den UI-Zustand
    var uiState by mutableStateOf(
        UnitConverterUiState(
            fromUnit = availableUnits.getOrNull(0)
                ?: error("Keine Einheiten definiert"),
            toUnit = availableUnits.getOrNull(1)
                ?: error("Weniger als zwei Einheiten definiert")
        )
    )
        private set

    // 3) History (per category)
    var unitHistory by mutableStateOf<List<UnitHistoryEntity>>(emptyList())
        private set

    init {
        loadHistory()
    }

    private fun loadHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = unitHistoryRepo.getHistory(category)
            unitHistory = list
        }
    }

    /** UI -> History eintragen (Kategorie wird intern verwendet) */
    suspend fun addToHistory(fromValue: String, fromUnit: String, toValue: String, toUnit: String) {
        unitHistoryRepo.insert(
            category = category,
            fromValue = fromValue,
            fromUnit = fromUnit,
            toValue = toValue,
            toUnit = toUnit,
            timestamp = System.currentTimeMillis()
        )
        loadHistory()
    }

    suspend fun clearUnitHistory() {
        unitHistoryRepo.clearHistory(category)
        loadHistory()
    }

    /** Für die Anzeige formatieren (wie Currency) */
    fun formatNumber(input: String, shortMode: Boolean): String =
        numberFormatService.formatNumber(input, shortMode, inputLine = false)

    /** 3. Einheitlicher Handler für Werteingaben */
    fun onValueChange(input: String) {
        uiState = if (uiState.selectedField == 1) {
            val newFrom = input
            val newTo = convert(newFrom, uiState.fromUnit, uiState.toUnit)
            uiState.copy(fromValue = newFrom, toValue = newTo)
        } else {
            val newTo = input
            val newFrom = convert(newTo, uiState.toUnit, uiState.fromUnit)
            uiState.copy(fromValue = newFrom, toValue = newTo)
        }
    }

    /** Handler für Änderung der "von"-Einheit */
    fun onFromUnitChanged(u: UnitDef) {
        uiState = if (uiState.selectedField == 1) {
            val newTo = convert(uiState.fromValue, u, uiState.toUnit)
            uiState.copy(fromUnit = u, toValue = newTo)
        } else {
            val newFrom = convert(uiState.toValue, uiState.toUnit, u)
            uiState.copy(fromUnit = u, fromValue = newFrom)
        }
    }

    /** Handler für Änderung der "zu"-Einheit */
    fun onToUnitChanged(u: UnitDef) {
        uiState = if (uiState.selectedField == 1) {
            val newTo = convert(uiState.fromValue, uiState.fromUnit, u)
            uiState.copy(toUnit = u, toValue = newTo)
        } else {
            val newFrom = convert(uiState.toValue, u, uiState.fromUnit)
            uiState.copy(toUnit = u, fromValue = newFrom)
        }
    }

    /** Handler für das Umschalten des selektierten Feldes */
    fun onSelectField(field: Int) {
        uiState = uiState.copy(selectedField = field)
    }

    /** Interne Konvertierungsfunktion – unverändert */
    private fun convert(input: String, a: UnitDef, b: UnitDef): String {
        val v = input.toBigDecimalOrNull() ?: return ""
        val mc = MathContext(18, RoundingMode.HALF_UP)

        val result: BigDecimal = when (category) {
            "Temperature" -> {
                // erst alles in Celsius
                val c = when (a.nameRes) {
                    R.string.UnitConvCatTemperature_Fahrenheit ->
                        v.subtract(BigDecimal("32"), mc)
                            .multiply(BigDecimal("5"), mc)
                            .divide(BigDecimal("9"), mc)
                    R.string.UnitConvCatTemperature_Kelvin ->
                        v.subtract(BigDecimal("273.15"), mc)
                    R.string.UnitConvCatTemperature_Rankine ->
                        v.subtract(BigDecimal("491.67"), mc)
                            .multiply(BigDecimal("5"), mc)
                            .divide(BigDecimal("9"), mc)
                    else -> v
                }
                // dann in Ziel umrechnen
                when (b.nameRes) {
                    R.string.UnitConvCatTemperature_Fahrenheit ->
                        c.multiply(BigDecimal("9"), mc)
                            .divide(BigDecimal("5"), mc)
                            .add(BigDecimal("32"), mc)
                    R.string.UnitConvCatTemperature_Kelvin ->
                        c.add(BigDecimal("273.15"), mc)
                    R.string.UnitConvCatTemperature_Rankine ->
                        c.add(BigDecimal("273.15"), mc)
                            .multiply(BigDecimal("9"), mc)
                            .divide(BigDecimal("5"), mc)
                    else -> c
                }
            }
            "FuelEconomy" -> {
                val mpgUS = BigDecimal("235.214583")
                val mpgUK = BigDecimal("282.481053")
                when (a.nameRes) {
                    R.string.UnitConvCatFuelEconomy_LitersPer100km -> when (b.nameRes) {
                        R.string.UnitConvCatFuelEconomy_MPG_US -> mpgUS.divide(v, mc)
                        R.string.UnitConvCatFuelEconomy_MPG_UK -> mpgUK.divide(v, mc)
                        else -> v
                    }
                    R.string.UnitConvCatFuelEconomy_MPG_US -> when (b.nameRes) {
                        R.string.UnitConvCatFuelEconomy_LitersPer100km -> mpgUS.divide(v, mc)
                        R.string.UnitConvCatFuelEconomy_MPG_UK ->
                            mpgUK.divide(mpgUS.divide(v, mc), mc)
                        else -> v
                    }
                    R.string.UnitConvCatFuelEconomy_MPG_UK -> when (b.nameRes) {
                        R.string.UnitConvCatFuelEconomy_LitersPer100km -> mpgUK.divide(v, mc)
                        R.string.UnitConvCatFuelEconomy_MPG_US ->
                            mpgUS.divide(mpgUK.divide(v, mc), mc)
                        else -> v
                    }
                    else -> v
                }
            }
            else -> {
                val factorA = a.factorToBase
                val factorB = b.factorToBase
                v.multiply(factorA, mc).divide(factorB, mc)
            }
        }

        return result.stripTrailingZeros().toPlainString()
    }

    /** Extension zum sicheren Parsen */
    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(this) } catch (_: Exception) { null }
}