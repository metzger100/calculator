package com.metzger100.calculator.ui.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metzger100.calculator.R
import com.metzger100.calculator.util.FeedbackManager

/**
 * Utility für Highlight- und Fade-Logik
 */
object KeyboardButtonStyle {
    fun isHighlighted(label: String): Boolean = when (label) {
        "C", "←", "%", "÷", "×", "−", "+", "=", "⇄" -> true
        else -> false
    }

    fun isFaded(label: String): Boolean = when (label) {
        "INV", "deg", "sin⁻¹", "cos⁻¹", "tan⁻¹", "lg",
        "10ˣ", "ln", "eˣ", "(", ")", "√", "x²", "x!(", "1/x",
        "π", "e", "sin", "cos", "tan", "^" -> true
        else -> false
    }
}

/**
 * Ein universeller Button für Standard- und Scientific-Keyboards.
 *
 * @param label       Das angezeigte Zeichen
 * @param fontSize    Schriftgröße (z.B. 24.sp in Standard, 20.sp in Scientific)
 * @param onClick     Callback für den Klick
 */
@Composable
fun KeyboardButton(
    label: String,
    modifier: Modifier = Modifier.Companion,
    fontSize: TextUnit = 24.sp,
    onClick: () -> Unit
) {
    val highlighted = KeyboardButtonStyle.isHighlighted(label)
    val faded       = KeyboardButtonStyle.isFaded(label)

    val feedbackManager = FeedbackManager.Companion.rememberFeedbackManager()
    val view = LocalView.current

    val buttonColor = when {
        highlighted -> MaterialTheme.colorScheme.primary
        faded       -> Color.Companion.Gray
        else        -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (highlighted)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    val contentDesc = getKeyContentDescription(label)

    Surface(
        modifier = modifier
            .clickable {
                feedbackManager.provideFeedback(view)
                onClick()
            }
            .semantics { contentDescription = contentDesc },
        color = buttonColor,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            contentAlignment = Alignment.Companion.Center,
            modifier = Modifier.Companion.fillMaxSize()
        ) {
            when (label) {
                "←" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                "⇄" -> Icon(Icons.Default.SwapHoriz, contentDescription = "Mode Switch")
                else -> Text(
                    text = label,
                    fontSize = fontSize,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun getKeyContentDescription(label: String): String {
    return when (label) {
        "C"    -> stringResource(R.string.key_clear)
        "←"    -> stringResource(R.string.key_backspace)
        "%"    -> stringResource(R.string.key_percent)
        "÷"    -> stringResource(R.string.key_divide)
        "×"    -> stringResource(R.string.key_multiply)
        "−"    -> stringResource(R.string.key_minus)
        "+"    -> stringResource(R.string.key_plus)
        "."    -> stringResource(R.string.key_decimal_point)
        "="    -> stringResource(R.string.key_equals)
        "⇄"    -> stringResource(R.string.key_switch_mode)
        // Scientific
        "INV"       -> stringResource(R.string.key_inverse)
        "deg"       -> stringResource(R.string.key_degree)
        "rad"       -> stringResource(R.string.key_radian)
        "^"         -> stringResource(R.string.key_power)
        "lg"        -> stringResource(R.string.key_log10)
        "10ˣ"       -> stringResource(R.string.key_exp10)
        "ln"        -> stringResource(R.string.key_ln)
        "eˣ"        -> stringResource(R.string.key_exp)
        "("         -> stringResource(R.string.key_left_paren)
        ")"         -> stringResource(R.string.key_right_paren)
        "√"         -> stringResource(R.string.key_sqrt)
        "x²"        -> stringResource(R.string.key_square)
        "1/x"       -> stringResource(R.string.key_reciprocal)
        "x!("       -> stringResource(R.string.key_factorial)
        "π"         -> stringResource(R.string.key_pi)
        "e"         -> stringResource(R.string.key_eulers_number)
        "sin"       -> stringResource(R.string.key_sin)
        "sin⁻¹"     -> stringResource(R.string.key_sin_inverse)
        "cos"       -> stringResource(R.string.key_cos)
        "cos⁻¹"     -> stringResource(R.string.key_cos_inverse)
        "tan"       -> stringResource(R.string.key_tan)
        "tan⁻¹"     -> stringResource(R.string.key_tan_inverse)
        else        -> label // fallback for digits/symbols
    }
}