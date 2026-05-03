package com.metzger100.calculator.util.format

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

data class FormatWithCursorMappingResult(
    val formatted: String,
    val inputToDisplay: IntArray, // inputPos → displayPos
    val displayToInput: IntArray  // displayPos → inputPos
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormatWithCursorMappingResult

        if (formatted != other.formatted) return false
        if (!inputToDisplay.contentEquals(other.inputToDisplay)) return false
        if (!displayToInput.contentEquals(other.displayToInput)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = formatted.hashCode()
        result = 31 * result + inputToDisplay.contentHashCode()
        result = 31 * result + displayToInput.contentHashCode()
        return result
    }
}

@Singleton
class NumberFormatService @Inject constructor() {

    private val smallRegex = Regex("""^(?<sign>-?)0*\.(?<zeros>0{5,})(?<digits>\d+)$""")
    private val lower = BigDecimal("0.001")
    private val upper = BigDecimal("1000000000")

    /**
     * Main number formatting method used for all screens.
     *
     * @param input Raw input string (number or expression)
     * @param shortMode If true: Currency short mode (always 2 decimals), else full mode
     * @param inputLine If true: Calculator input line stays unprocessed for better readability
     */
    fun formatNumber(input: String, shortMode: Boolean, inputLine: Boolean): String {
        // Do not format the Calculator input line, only process ResultPreview
        if (inputLine) {
            val base = input.ifEmpty { "0" }
            return replaceOperatorsOrMap(base, withMapping = false) as String
        }

        // 1) Try to parse as number or early return
        val bigDec = try {
            BigDecimal(input)
        } catch (_: NumberFormatException) {
            val base = input.ifEmpty { "0" }
            return replaceOperatorsOrMap(base, withMapping = false) as String
        }

        // 2) Special case: Zero
        if (bigDec.signum() == 0) return "0"

        // 3) Special case: Many leading zeros after decimal point → exponential format
        smallRegex.matchEntire(input)?.let { m ->
            val sign = m.groups["sign"]?.value.orEmpty()
            val zeroCount = m.groups["zeros"]?.value?.length ?: 0
            val digits = m.groups["digits"]?.value.orEmpty()
            val exponent = -(zeroCount + 1)
            val mantissa = if (digits.length > 1)
                "${digits[0]}.${digits.substring(1)}"
            else
                digits
            val sci = "$sign$mantissa×10${toSuperscript(exponent)}"
            return replaceOperatorsOrMap(sci, withMapping = false) as String
        }

        // 4) Normal number formatting
        val formatted = if (shortMode) {
            // Currency short mode: always 2 decimals
            val rounded = bigDec.setScale(2, RoundingMode.HALF_UP)
            groupOrSci(rounded)
        } else {
            // Full mode: No rounding, scientific notation only for extreme values
            groupOrSci(bigDec.stripTrailingZeros())
        }

        // 5) Operator replacement (only relevant for Calculator)
        return replaceOperators(formatted)
    }

    /**
     * Formats input and provides a mapping from input cursor to display cursor and back.
     */
    fun formatNumberWithCursorMapping(
        input: String
    ): FormatWithCursorMappingResult {
        val base = input.ifEmpty { "0" }
        return replaceOperatorsWithMapping(base)
    }

    /** Groups numbers or returns scientific notation if appropriate */
    private fun groupOrSci(
        bd: BigDecimal
    ): String {
        val absBd = bd.abs()
        val norm = bd.stripTrailingZeros()
        val prec = norm.precision()
        val scale = norm.scale()
        val exp = prec - scale - 1
        val unscaled = norm.unscaledValue().abs().toString()

        val useSci = ((absBd < lower && exp >= 6) || (absBd > upper && scale < 0))

        return if (useSci) buildSci(bd.signum(), unscaled, exp)
        else groupIntegerPart(bd.toPlainString())
    }

    /** Builds mantissa×10^exp with superscript exponent */
    private fun buildSci(signum: Int, unscaled: String, exp: Int): String {
        val mantissa = if (unscaled.length > 1)
            "${unscaled[0]}.${unscaled.substring(1)}"
        else
            unscaled
        val signChar = if (signum < 0) "-" else ""
        return "$signChar$mantissa×10${toSuperscript(exp)}"
    }

    /** Groups integer part in blocks of three, leaves decimals unchanged */
    private fun groupIntegerPart(orig: String): String {
        val negative = orig.startsWith("-")
        val parts = orig.trimStart('-').split('.', limit = 2)
        val intPart = parts[0].ifEmpty { "0" }
        val frac = parts.getOrNull(1)
        val grouped = intPart.reversed().chunked(3).joinToString(" ").reversed()

        return buildString {
            if (negative) append('-')
            append(grouped)
            if (frac != null) append('.').append(frac)
        }
    }

    /** Converts an integer exponent to unicode superscript, e.g., -6 → ⁻⁶ */
    private fun toSuperscript(exp: Int): String {
        val sup = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹', '-' to '⁻'
        )
        return exp.toString().map { sup[it] ?: it }.joinToString("")
    }

    /**
     * Shared replacement logic for both display replacement and cursor mapping.
     * If withMapping=false: Only returns replaced string (like replaceOperators).
     * If withMapping=true: Returns FormatWithCursorMappingResult with full cursor mapping.
     */

    private fun replaceOperators(input: String): String =
        replaceOperatorsOrMap(input, withMapping = false) as String

    private fun replaceOperatorsWithMapping(input: String): FormatWithCursorMappingResult =
        replaceOperatorsOrMap(input, withMapping = true) as FormatWithCursorMappingResult

    private fun replaceOperatorsOrMap(input: String, withMapping: Boolean): Any {
        if (!withMapping) {
            // Simple string replacement
            var result = input
            for ((from, to) in replacements) {
                result = result.replace(from, to)
            }
            return result
        } else {
            // Replacement + mapping for cursor positions
            val inputToDisplay = IntArray(input.length + 1)
            val displayToInputList = mutableListOf<Int>()
            var inPos = 0
            var outPos = 0
            val out = StringBuilder()
            while (inPos < input.length) {
                var replaced = false
                for ((from, to) in replacements) {
                    if (input.startsWith(from, inPos)) {
                        repeat(from.length) {
                            inputToDisplay[inPos + it] = outPos
                        }
                        repeat(to.length) {
                            displayToInputList.add(inPos)
                        }
                        out.append(to)
                        inPos += from.length
                        outPos += to.length
                        replaced = true
                        break
                    }
                }
                if (!replaced) {
                    inputToDisplay[inPos] = outPos
                    displayToInputList.add(inPos)
                    out.append(input[inPos])
                    inPos += 1
                    outPos += 1
                }
            }
            inputToDisplay[input.length] = outPos
            displayToInputList.add(input.length)
            return FormatWithCursorMappingResult(
                out.toString(),
                inputToDisplay,
                displayToInputList.toIntArray()
            )
        }
    }

    private val replacements = listOf(
        "RECIPROCAL(" to "1/(",
        "EXP(" to "e^(",
        "PI()" to "π",
        "E()" to "e",
        "SQRT(" to "√(",
        "FACT(" to "x!(",
        "LOG10(" to "lg(",
        "LOG(" to "ln(",
        "ASINR(" to "sin⁻¹(",
        "ACOSR(" to "cos⁻¹(",
        "ATANR(" to "tan⁻¹(",
        "ASIN(" to "sin⁻¹(",
        "ACOS(" to "cos⁻¹(",
        "ATAN(" to "tan⁻¹(",
        "SINR(" to "sin(",
        "COSR(" to "cos(",
        "TANR(" to "tan(",
        "SIN(" to "sin(",
        "COS(" to "cos(",
        "TAN(" to "tan(",
        "/" to "÷",
        "*" to "×",
        "-" to "−"
    )
}
