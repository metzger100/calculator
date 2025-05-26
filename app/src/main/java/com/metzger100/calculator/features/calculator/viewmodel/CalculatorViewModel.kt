package com.metzger100.calculator.features.calculator.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezylang.evalex.bigmath.BigMathExpression
import com.ezylang.evalex.config.ExpressionConfiguration
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.CalculationEntity
import com.metzger100.calculator.data.repository.CalculationRepository
import com.metzger100.calculator.features.calculator.model.CalculatorMode
import com.metzger100.calculator.util.format.NumberFormatService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

private val ATOMIC_FUNCTION_TOKENS = setOf(
    // Trigonometric (degree/radian)
    "SIN(", "COS(", "TAN(",
    "ASIN(", "ACOS(", "ATAN(",
    "SINR(", "COSR(", "TANR(",
    "ASINR(", "ACOSR(", "ATANR(",
    // Other unary functions
    "SQRT(", "LOG(", "LOG10(", "EXP(",
    "RECIPROCAL(", "FACT(",
    // Constants
    "PI()", "E()"
)

private const val MAX_FACTORIAL = 100

data class CalculatorUiState(
    val input: String = "",
    val tokens: List<String> = emptyList(),
    val cursor: Int = 0,
    val preview: String = "",
    val mode: CalculatorMode = CalculatorMode.STANDARD,
    val isDegree: Boolean = true,
    val inverse: Boolean = false
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CalculationRepository,
    val numberFormatService: NumberFormatService,
    private val application: Application
) : ViewModel() {

    var history by mutableStateOf(listOf<CalculationEntity>())
        private set

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    var forceRefresh by mutableStateOf(false)
        private set

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch { history = repository.getHistory() }
    }

    fun formatNumber(
        input: String,
        shortMode: Boolean,
        inputLine: Boolean
    ): String = numberFormatService.formatNumber(input, shortMode, inputLine)

    fun tokenizeInput(input: String): List<String> {
        if (input.isBlank()) return emptyList()
        val tokens = mutableListOf<String>()
        var i = 0
        val atomic = ATOMIC_FUNCTION_TOKENS.sortedByDescending { it.length }

        while (i < input.length) {
            val match = atomic.firstOrNull { input.startsWith(it, i) }
            if (match != null) {
                tokens.add(match)
                i += match.length
                continue
            }
            val c = input[i]
            if (c.isDigit() || c == '.') {
                val start = i
                var dotFound = (c == '.')
                i++
                while (i < input.length &&
                    (input[i].isDigit() || (!dotFound && input[i] == '.'))
                ) {
                    if (input[i] == '.') dotFound = true
                    i++
                }
                tokens.add(input.substring(start, i))
            } else {
                tokens.add(c.toString())
                i++
            }
        }
        return tokens
    }

    fun onCursorChange(requestedPos: Int) {
        val trimmedPos = requestedPos.coerceIn(0, uiState.input.length)
        var charCount = 0
        var adjustedPos = trimmedPos
        uiState.tokens.forEach { tok ->
            val start = charCount
            val end = charCount + tok.length

            if (tok in ATOMIC_FUNCTION_TOKENS && adjustedPos > start && adjustedPos < end) {
                val distToStart = adjustedPos - start
                val distToEnd = end - adjustedPos
                adjustedPos = if (distToStart < distToEnd) start else end
                return@forEach
            }
            charCount = end
        }
        uiState = uiState.copy(cursor = adjustedPos)
    }

    fun toggleDegreeMode() {
        val newIsDeg = !uiState.isDegree

        var charCount = 0
        var tokenIndex = 0
        var offsetInToken = 0
        for ((i, tok) in uiState.tokens.withIndex()) {
            val start = charCount
            val end = start + tok.length
            if (uiState.cursor <= end) {
                tokenIndex = i
                offsetInToken = uiState.cursor - start
                break
            }
            charCount = end
        }

        val newTokens = uiState.tokens.map { token ->
            when (token) {
                "SIN("   -> if (newIsDeg) "SIN("  else "SINR("
                "COS("   -> if (newIsDeg) "COS("  else "COSR("
                "TAN("   -> if (newIsDeg) "TAN("  else "TANR("
                "ASIN("  -> if (newIsDeg) "ASIN(" else "ASINR("
                "ACOS("  -> if (newIsDeg) "ACOS(" else "ACOSR("
                "ATAN("  -> if (newIsDeg) "ATAN(" else "ATANR("
                "SINR("  -> if (newIsDeg) "SIN("  else "SINR("
                "COSR("  -> if (newIsDeg) "COS("  else "COSR("
                "TANR("  -> if (newIsDeg) "TAN("  else "TANR("
                "ASINR(" -> if (newIsDeg) "ASIN(" else "ASINR("
                "ACOSR(" -> if (newIsDeg) "ACOS(" else "ACOSR("
                "ATANR(" -> if (newIsDeg) "ATAN(" else "ATANR("
                else       -> token
            }
        }
        val newInput = newTokens.joinToString("")

        var newCharCount = 0
        var newCursor = 0
        for ((i, tok) in newTokens.withIndex()) {
            if (i == tokenIndex) {
                newCursor = if (offsetInToken == uiState.tokens[tokenIndex].length) {
                    newCharCount + tok.length
                } else {
                    newCharCount + offsetInToken.coerceAtMost(tok.length)
                }
                break
            }
            newCharCount += tok.length
        }

        uiState = uiState.copy(
            isDegree = newIsDeg,
            tokens = newTokens,
            input = newInput,
            cursor = newCursor,
            preview = updatePreviewResults(newInput, newIsDeg)
        )
    }


    fun toggleMode() {
        val newMode = if (uiState.mode == CalculatorMode.STANDARD) CalculatorMode.SCIENTIFIC else CalculatorMode.STANDARD
        uiState = uiState.copy(mode = newMode)
    }

    fun toggleInverse() {
        uiState = uiState.copy(inverse = !uiState.inverse)
    }

    // Insert token at cursor, splitting tokens if needed
    fun onInput(token: String) {
        val newTokens = uiState.tokens.toMutableList()
        val pos = uiState.cursor
        var charCount = 0
        var inserted = false

        for ((i, tok) in uiState.tokens.withIndex()) {
            val start = charCount
            val end = charCount + tok.length
            if (pos <= end) {
                // inside or at end of this token
                val within = pos - start
                if (within in 1 until tok.length) {
                    // split token
                    val left = tok.substring(0, within)
                    val right = tok.substring(within)
                    newTokens[i] = left
                    newTokens.add(i + 1, token)
                    newTokens.add(i + 2, right)
                } else {
                    // at boundary: either at start or end
                    val insertIdx = if (within == 0) i else i + 1
                    newTokens.add(insertIdx, token)
                }
                inserted = true
                break
            }
            charCount = end
        }
        if (!inserted) {
            // at very end
            newTokens.add(token)
        }
        // rebuild input string and move cursor
        val newInput = newTokens.joinToString("")
        val newCursor = pos + token.length
        uiState = uiState.copy(
            tokens = newTokens,
            cursor = newCursor,
            input = newInput,
            preview = updatePreviewResults(newInput)
        )
    }

    fun backspace() {
        if (uiState.cursor == 0) return
        val newTokens = uiState.tokens.toMutableList()
        var charCount = 0
        var newCursor = uiState.cursor

        for ((i, tok) in uiState.tokens.withIndex()) {
            val start = charCount
            val end = charCount + tok.length
            if (uiState.cursor <= end) {
                val within = uiState.cursor - start
                if (tok in ATOMIC_FUNCTION_TOKENS) {
                    newTokens.removeAt(i)
                    newCursor = start
                } else if (tok.length > 1) {
                    val removeAt = within - 1
                    if (removeAt in tok.indices) {
                        val newTok = tok.removeRange(removeAt, removeAt + 1)
                        if (newTok.isNotEmpty()) {
                            newTokens[i] = newTok
                        } else {
                            newTokens.removeAt(i)
                        }
                        newCursor--
                    }
                } else {
                    newTokens.removeAt(i)
                    newCursor--
                }
                break
            }
            charCount = end
        }

        val newInput = newTokens.joinToString("")
        uiState = uiState.copy(
            tokens = newTokens,
            cursor = newCursor.coerceAtLeast(0),
            input = newInput,
            preview = updatePreviewResults(newInput)
        )
    }


    fun clear() {
        uiState = uiState.copy(tokens = emptyList(), cursor = 0, input = "", preview = "")
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            history = emptyList()
            forceRefresh = !forceRefresh
        }
    }

    fun calculate() {
        try {
            Log.d("CalculatorViewModel", "Calculating: ${uiState.input}")
            if (!validateExpression(uiState.input)) {
                uiState = uiState.copy(preview = application.getString(R.string.Calculator_Error))
                return
            }
            val config = buildConfig()
            val expr = BigMathExpression(uiState.input, config)
            val result = expr.evaluate().numberValue
            if (result != null) {
                val resultStr = BigDecimal(result.toString()).stripTrailingZeros().toPlainString()
                viewModelScope.launch {
                    repository.insert(uiState.input, resultStr)
                    history = repository.getHistory()
                }
                uiState = uiState.copy(
                    tokens = listOf(resultStr),
                    cursor = resultStr.length,
                    input = resultStr,
                    preview = ""
                )
                Log.d("CalculatorViewModel", "Result: $resultStr")
            } else {
                uiState = uiState.copy(tokens = listOf("0"), cursor = 1, input = "0", preview = "")
            }
        } catch (e: Exception) {
            uiState = uiState.copy(preview = application.getString(R.string.Calculator_Error))
            Log.e("CalculatorViewModel", "Error during calculate")
        }
    }

    private fun updatePreviewResults(input: String, isDeg: Boolean = uiState.isDegree): String {
        if (input.isBlank()) return ""
        if (!validateExpression(input, isDeg)) {
            return application.getString(R.string.Calculator_Error)
        }
        return try {
            val config = buildConfig()
            val expr = BigMathExpression(input, config)
            expr.evaluate().numberValue
                ?.toString()
                ?.let { formatResult(it) } ?: ""
        } catch (e: Exception) {
            application.getString(R.string.Calculator_Error)
        }
    }

    /** Baut die EvalEx‑Konfiguration mit gewünschter Genauigkeit und Standard‑Funktionen. */
    private fun buildConfig(): ExpressionConfiguration {
        return ExpressionConfiguration.builder()
            .decimalPlacesResult(16)
            .mathContext(MathContext(18, RoundingMode.HALF_UP))
            .build()
    }

    /** Prüft, ob der gesamte Ausdruck in allen reellen Domains gültig ist. */
    private fun validateExpression(input: String, isDeg: Boolean = uiState.isDegree): Boolean {
        val tag = "CalculatorViewModel"
        Log.d(tag, "validateExpression: checking input=\"$input\"")

        val config = buildConfig()

        if (input.trim() == "0^0") {
            Log.w(tag, "Invalid: 0^0 is undefined")
            return false
        }

        if (!validateFactorials(input, config)) {
            Log.w(tag, "Invalid: factorial validation failed")
            return false
        }

        if (!validateTanDomain(input, config, isDeg)) {
            Log.w(tag, "Invalid: tangent domain validation failed")
            return false
        }

        if (!validateUnaryDomain(input, "SQRT", BigDecimal.ZERO, allowEqual = true, config)) {
            Log.w(tag, "Invalid: square root domain validation failed")
            return false
        }
        if (!validateUnaryDomain(input, "LOG", BigDecimal.ZERO, allowEqual = false, config)) {
            Log.w(tag, "Invalid: LOG argument must be greater than 0")
            return false
        }
        if (!validateUnaryDomain(input, "LOG10", BigDecimal.ZERO, allowEqual = false, config)) {
            Log.w(tag, "Invalid: LOG10 argument must be greater than 0")
            return false
        }

        Log.d(tag, "validateExpression: input is valid")
        return true
    }

    /** Extrahiert alle inneren Ausdrücke von FUNKTION(...) inklusive verschachtelter Klammern. */
    private fun extractArgs(expr: String, functionName: String): List<String> {
        val args = mutableListOf<String>()
        var idx = 0
        while (true) {
            val start = expr.indexOf("$functionName(", idx).takeIf { it >= 0 } ?: break
            var depth = 1
            var pos = start + functionName.length + 1
            while (pos < expr.length && depth > 0) {
                when (expr[pos]) {
                    '(' -> depth++
                    ')' -> depth--
                }
                pos++
            }
            if (depth == 0) {
                args += expr.substring(start + functionName.length + 1, pos - 1)
                idx = pos
            } else {
                break
            }
        }
        return args
    }

    /** Prüft unäre Funktionen (SQRT, LOG, LOG10) auf ihren Definitionsbereich. */
    private fun validateUnaryDomain(
        input: String,
        functionName: String,
        bound: BigDecimal,
        allowEqual: Boolean,
        config: ExpressionConfiguration
    ): Boolean {
        for (inner in extractArgs(input, functionName)) {
            val v = try {
                BigMathExpression(inner, config).evaluate().numberValue ?: return false
            } catch (_: Exception) {
                return false
            }
            if (allowEqual) {
                if (v < bound) return false
            } else {
                if (v <= bound) return false
            }
        }
        return true
    }

    /** Prüft TAN‑Polstellen über cos(inner) != 0 mit adaptivem Epsilon (ULP). */
    private fun validateTanDomain(
        input: String,
        config: ExpressionConfiguration,
        isDegree: Boolean
    ): Boolean {
        val fn = if (isDegree) "TAN" else "TANR"
        val cosFn = if (isDegree) "COS" else "COSR"
        for (inner in extractArgs(input, fn)) {
            val cosVal = try {
                BigMathExpression("$cosFn($inner)", config).evaluate().numberValue ?: return false
            } catch (_: Exception) {
                return false
            }
            val eps = cosVal.ulp().multiply(BigDecimal(2))
            if (cosVal.abs() <= eps) return false
        }
        return true
    }

    private fun validateFactorials(expression: String, config: ExpressionConfiguration): Boolean {
        var expr = expression
        while (true) {
            val idx = expr.indexOf("FACT(")
            if (idx == -1) break
            var open = 1; var pos = idx + 5
            while (pos < expr.length && open > 0) {
                when (expr[pos]) {
                    '(' -> open++
                    ')' -> open--
                }
                pos++
            }
            if (open != 0) return false
            val inner = expr.substring(idx + 5, pos - 1)
            if (!validateFactorials(inner, config)) return false
            try {
                val valInner = BigMathExpression(inner, config).evaluate().numberValue
                if (valInner == null ||
                    valInner < BigDecimal.ZERO ||
                    valInner.stripTrailingZeros().scale() > 0
                ) return false
                if (valInner > BigDecimal(MAX_FACTORIAL)) return false
            } catch (_: Exception) {
                return false
            }
            expr = expr.removeRange(idx, pos)
        }
        return true
    }

    private fun formatResult(raw: String): String =
        try {
            BigDecimal(raw).stripTrailingZeros().toPlainString()
        } catch (_: NumberFormatException) {
            raw
        }
}