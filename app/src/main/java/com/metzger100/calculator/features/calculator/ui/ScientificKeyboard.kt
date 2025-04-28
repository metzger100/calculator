package com.metzger100.calculator.features.calculator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScientificKeyboard(
    inverse: Boolean,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onEquals: () -> Unit,
    onToggleMode: () -> Unit,
    onToggleInverse: () -> Unit,
    onToggleDegreeMode: () -> Unit,
    isDegreeMode: Boolean
) {
    val buttonSpacing = 6.dp

    val buttons = listOf(
        listOf("INV", if (isDegreeMode) "deg" else "rad", if (inverse) "sin⁻¹" else "sin", if (inverse) "cos⁻¹" else "cos", if (inverse) "tan⁻¹" else "tan"),
        listOf("^", if (inverse) "10ˣ" else "lg", if (inverse) "eˣ" else "ln", "(", ")"),
        listOf(if (inverse) "x²" else "√", "C", "←", "%", "÷"),
        listOf("x!(", "7", "8", "9", "×"),
        listOf("1/x", "4", "5", "6", "−"),
        listOf("π", "1", "2", "3", "+"),
        listOf("⇄", "e", "0", ".", "=")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    row.forEach { label ->
                        ScientificButton(
                            label = label,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (label) {
                                    "INV" -> onToggleInverse()
                                    "deg", "rad" -> onToggleDegreeMode()  // Schalte zwischen Grad und Rad
                                    "C" -> onClear()
                                    "←" -> onBack()
                                    "=" -> onEquals()
                                    "⇄" -> onToggleMode()
                                    else -> onInput(mapScientificSymbol(label, isDegreeMode))
                                }
                            },
                            isHighlighted = isHighlightedButton(label),
                            isFaded = isFadedButton(label)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScientificButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isHighlighted: Boolean,
    isFaded: Boolean
) {
    val buttonColor = when {
        isHighlighted -> MaterialTheme.colorScheme.primary
        isFaded -> Color.Gray
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isHighlighted) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val surfaceModifier = modifier
        .clickable { onClick() }

    Surface(
        modifier = surfaceModifier
            .clickable { onClick() },
        color = buttonColor,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (label) {
                "←" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                "⇄" -> Icon(Icons.Default.SwapHoriz, contentDescription = "Mode Switch")
                else -> Text(
                    text = label,
                    fontSize = 20.sp,
                    color = textColor
                )
            }
        }
    }
}

fun mapScientificSymbol(symbol: String, isDegreeMode: Boolean): String {
    return when {
        isDegreeMode -> { // Grad-Modus: keine Änderung an trigonometrischen Funktionen
            when (symbol) {
                "÷" -> "/"
                "×" -> "*"
                "−" -> "-"
                "π" -> "PI"
                "^" -> "^"
                "√" -> "SQRT("
                "x²" -> "^2"
                "1/x" -> "1/("
                "x!(" -> "FACT("
                "lg" -> "LOG10("
                "10ˣ" -> "10^"
                "ln" -> "LOG("
                "eˣ" -> "e^"
                "sin" -> "SIN("
                "cos" -> "COS("
                "tan" -> "TAN("
                "sin⁻¹" -> "ASIN("
                "cos⁻¹" -> "ACOS("
                "tan⁻¹" -> "ATAN("
                else -> symbol
            }
        }
        else -> { // Rad-Modus: trigonometrische Funktionen mit "R" für Radians umwandeln
            when (symbol) {
                "÷" -> "/"
                "×" -> "*"
                "−" -> "-"
                "π" -> "PI"
                "^" -> "^"
                "√" -> "SQRT("
                "x²" -> "^2"
                "1/x" -> "1/("
                "x!(" -> "FACT("
                "lg" -> "LOG10("
                "10ˣ" -> "10^"
                "ln" -> "LOG("
                "eˣ" -> "e^"
                "sin" -> "SINR("
                "cos" -> "COSR("
                "tan" -> "TANR("
                "sin⁻¹" -> "ASINR("
                "cos⁻¹" -> "ACOSR("
                "tan⁻¹" -> "ATANR("
                else -> symbol
            }
        }
    }
}