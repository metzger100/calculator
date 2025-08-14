// com.metzger100.calculator.features.unit.viewmodel.UnitConverterViewModel.kt
package com.metzger100.calculator.features.unit.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity
import com.metzger100.calculator.data.repository.UnitHistoryRepository
import com.metzger100.calculator.features.unit.ui.UnitConverterConstants
import com.metzger100.calculator.features.unit.ui.UnitConverterConstants.UnitDef
import com.metzger100.calculator.util.format.NumberFormatService
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val unitHistoryRepo: UnitHistoryRepository,
    private val application: Application,
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

    private var appliedInitUnits = false

    private fun loadHistory() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { unitHistoryRepo.getHistory(category) }
            unitHistory = list
            if (!appliedInitUnits) {
                applyUnitsFromHistoryOrDefaults(list)
                appliedInitUnits = true
            }
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

    /** Resolve the UI label (string) for a UnitDef using current locale. */
    private fun labelFor(u: UnitDef): String = application.getString(u.nameRes)

    /** Try to find a UnitDef by the label text we stored in history (locale-dependent). */
    private fun findUnitByLabel(label: String): UnitDef? =
        availableUnits.firstOrNull { labelFor(it).equals(label, ignoreCase = true) }

    /** Safe "pick by @StringRes" from availableUnits, or null if that id isn't present. */
    private fun pick(@StringRes id: Int): UnitDef? =
        availableUnits.firstOrNull { it.nameRes == id }

    /** Choose sensible defaults for this category if there is no history yet. */
    private fun defaultUnitsForCategory(): Pair<UnitDef, UnitDef> {
        // If any of these is missing in availableUnits (shouldn't happen), fall back to first two units.
        return when (category) {
            "Length"       -> (pick(R.string.UnitConvCatLength_Meter) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatLength_Foot)  ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Weight"       -> (pick(R.string.UnitConvCatWeight_Kilogram) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatWeight_Pound)    ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Volume"       -> (pick(R.string.UnitConvCatVolume_Liter)     ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatVolume_GallonUS)  ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Area"         -> (pick(R.string.UnitConvCatArea_SquareMeter) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatArea_SquareFoot)  ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Temperature"  -> (pick(R.string.UnitConvCatTemperature_Celsius)    ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatTemperature_Fahrenheit) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Time"         -> (pick(R.string.UnitConvCatTime_Millisecond) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatTime_Minute) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Speed"        -> (pick(R.string.UnitConvCatSpeed_KilometerPerHour) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatSpeed_MilePerHour)      ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Energy"       -> (pick(R.string.UnitConvCatEnergy_Joule)        ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatEnergy_KilowattHour) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Power"        -> (pick(R.string.UnitConvCatPower_Kilowatt)    ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatPower_Horsepower) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Pressure"     -> (pick(R.string.UnitConvCatPressure_Bar)  ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatPressure_PSI)  ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Frequency"    -> (pick(R.string.UnitConvCatFrequency_Hertz)    ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatFrequency_Kilohertz) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Data"         -> (pick(R.string.UnitConvCatData_Megabyte) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatData_Mebibyte) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "FuelEconomy"  -> (pick(R.string.UnitConvCatFuelEconomy_LitersPer100km) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatFuelEconomy_MPG_US)         ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "PlaneAngle"   -> (pick(R.string.UnitConvCatPlaneAngle_Degree) ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatPlaneAngle_Radian) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            "Amount"       -> (pick(R.string.UnitConvCatAmount_Mole)      ?: availableUnits.first()) to
                    (pick(R.string.UnitConvCatAmount_Millimole) ?: availableUnits.getOrNull(1) ?: availableUnits.first())

            else           -> availableUnits.getOrNull(0) to availableUnits.getOrNull(1)
        }.let { (a, b) ->
            // final fallback if any is null
            val first  = a ?: availableUnits.first()
            val second = b ?: availableUnits.getOrNull(1) ?: first
            first to second
        }
    }

    /** Apply a unit pair to the UI state and recalc the dependent field. */
    private fun applyUnits(from: UnitDef, to: UnitDef) {
        uiState = if (uiState.selectedField == 1) {
            val newToVal = convert(uiState.fromValue, from, to)
            uiState.copy(fromUnit = from, toUnit = to, toValue = newToVal)
        } else {
            val newFromVal = convert(uiState.toValue, to, from)
            uiState.copy(fromUnit = from, toUnit = to, fromValue = newFromVal)
        }
    }

    /** Prefer last history pair; otherwise defaults. */
    private fun applyUnitsFromHistoryOrDefaults(history: List<UnitHistoryEntity>) {
        val last = history.lastOrNull()
        if (last != null) {
            val fromU = findUnitByLabel(last.fromUnit)
            val toU   = findUnitByLabel(last.toUnit)
            if (fromU != null && toU != null) {
                applyUnits(fromU, toU)
                return
            }
        }
        val (defFrom, defTo) = defaultUnitsForCategory()
        applyUnits(defFrom, defTo)
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