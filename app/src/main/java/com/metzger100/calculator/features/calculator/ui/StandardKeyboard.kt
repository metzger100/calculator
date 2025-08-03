package com.metzger100.calculator.features.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metzger100.calculator.ui.keyboard.KeyboardButton

@Composable
fun StandardKeyboard(
    modifier: Modifier = Modifier,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onEquals: () -> Unit,
    onToggleMode: () -> Unit
) {
    val buttonSpacing = 6.dp
    val buttons = listOf(
        listOf("C", "←", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("⇄", "0", ".", "=")
    )

    // Extrahierter Modifier für das gesamte Layout
    val columnModifier = Modifier
        .fillMaxSize()
        .padding(top = 4.dp)

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Jede Zeile gleich hoch
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    row.forEach { label ->
                        // Reuse des gemeinsamen Modifiers
                        val buttonModifier = Modifier
                            .weight(1f)

                        KeyboardButton(
                            label = label,
                            modifier = buttonModifier,
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    "←" -> onBack()
                                    "=" -> onEquals()
                                    "⇄" -> onToggleMode()
                                    else -> onInput(mapSymbol(label))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

fun mapSymbol(symbol: String): String {
    return when (symbol) {
        "÷" -> "/"
        "×" -> "*"
        "−" -> "-"
        else -> symbol
    }
}