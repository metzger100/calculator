// com.metzger100.calculator.features.unit.ui.UnitConverterKeyboard.kt
package com.metzger100.calculator.features.unit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metzger100.calculator.util.FeedbackManager

@Composable
fun UnitConverterKeyboard(
    modifier: Modifier = Modifier,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onEquals: () -> Unit
) {
    val buttonSpacing = 6.dp
    val buttons = listOf(
        listOf("7", "8", "9", "C"),
        listOf("4", "5", "6", "←"),
        listOf("1", "2", "3", ""),
        listOf("00", "0", ".", "=")
    )

    Box(
        modifier = modifier.fillMaxWidth()
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
                        UnitConverterButton(
                            label = label,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    "←" -> onBack()
                                    "=" -> onEquals()
                                    else -> onInput(label)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConverterButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val buttonColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current

    Surface(
        modifier = modifier.clickable {
            feedbackManager.provideFeedback(view)
            onClick()
        },
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
                else -> Text(
                    text = label,
                    fontSize = 24.sp,
                    color = textColor
                )
            }
        }
    }
}