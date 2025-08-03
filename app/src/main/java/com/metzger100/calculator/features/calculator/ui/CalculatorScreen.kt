package com.metzger100.calculator.features.calculator.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.CalculationEntity
import com.metzger100.calculator.features.calculator.model.CalculatorMode
import com.metzger100.calculator.features.calculator.viewmodel.CalculatorViewModel
import com.metzger100.calculator.util.FeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    openKeyboardOnStart: Boolean = false,
    scientificOnStart: Boolean = false
) {
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current

    var modeInitialized by remember { mutableStateOf(false) }
    var keyboardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(scientificOnStart, viewModel, modeInitialized) {
        if (!modeInitialized) {
            if (scientificOnStart && viewModel.uiState.mode != CalculatorMode.SCIENTIFIC) {
                viewModel.toggleMode()
            }
            modeInitialized = true
        }
    }

    LaunchedEffect(openKeyboardOnStart) {
        if (openKeyboardOnStart) {
            keyboardVisible = true
        }
    }
    val uiState by viewModel::uiState

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val blinkAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val reversedHistory by remember(viewModel.history) {
        derivedStateOf { viewModel.history.reversed() }
    }

    val clipboard = LocalClipboard.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val keyboardHeight = maxHeight * 0.5f
        val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
        val resultColor = MaterialTheme.colorScheme.primary.toArgb()

        Column(modifier = Modifier.fillMaxSize()) {
            // History list with RecyclerView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                val context = LocalContext.current
                AndroidView(
                    factory = {
                        RecyclerView(context).apply {
                            layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                            adapter = CalculationAdapter(viewModel, textColor, resultColor)
                            clipToPadding = true
                            clipChildren = true
                        }
                    },
                    update = { rv ->
                        val adapter = rv.adapter as CalculationAdapter
                        adapter.updateData(reversedHistory)
                        if (reversedHistory.isNotEmpty()) rv.scrollToPosition(0)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input & preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            feedbackManager.provideFeedback(view)
                            keyboardVisible = !keyboardVisible
                        }) {
                            val desc = if (keyboardVisible)
                                stringResource(R.string.hide_keyboard)
                            else
                                stringResource(R.string.show_keyboard)
                            Icon(
                                imageVector = if (keyboardVisible)
                                    Icons.Default.KeyboardArrowDown
                                else
                                    Icons.Default.KeyboardArrowUp,
                                contentDescription = desc,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            val formatRes = viewModel.numberFormatService.formatNumberWithCursorMapping(
                                uiState.input
                            )
                            val formattedInput = formatRes.formatted
                            val safeInputCursor = uiState.cursor.coerceIn(0, formatRes.inputToDisplay.size - 1)
                            val displayCursor = formatRes.inputToDisplay[safeInputCursor].coerceIn(0, formattedInput.length)

                            Box(modifier = Modifier.fillMaxWidth()) {
                                BasicTextField(
                                    value = TextFieldValue(
                                        text = formattedInput,
                                        selection = TextRange(displayCursor)
                                    ),
                                    onValueChange = {
                                        val newDisplayCursor = it.selection.start.coerceIn(0, formatRes.displayToInput.size - 1)
                                        val newInputCursor = formatRes.displayToInput[newDisplayCursor].coerceIn(0, uiState.input.length)
                                        viewModel.onCursorChange(newInputCursor)
                                    },
                                    readOnly = true,
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End,
                                        letterSpacing = 3.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    onTextLayout = { layoutResult = it }
                                )

                                if (formattedInput != "0") {
                                    layoutResult?.let { textLayout ->
                                        runCatching { textLayout.getCursorRect(displayCursor) }
                                            .getOrNull()
                                            ?.let { cursorRect ->
                                                val canvasColor = MaterialTheme.colorScheme.onSurface
                                                val extraOffsetPx = with(LocalDensity.current) {
                                                    if (displayCursor == formattedInput.length) 2.dp.toPx() else 0f
                                                }
                                                Canvas(modifier = Modifier.matchParentSize()) {
                                                    drawLine(
                                                        color = canvasColor,
                                                        start = Offset(cursorRect.left + extraOffsetPx, cursorRect.top),
                                                        end = Offset(cursorRect.left + extraOffsetPx, cursorRect.bottom),
                                                        strokeWidth = 2.dp.toPx(),
                                                        alpha = blinkAlpha
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                            if (uiState.preview.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "= ${viewModel.formatNumber(
                                        uiState.preview,
                                        shortMode = false,
                                        inputLine = false
                                    )}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (uiState.input.isNotEmpty() && uiState.preview.isEmpty()) {
                            val desc = stringResource(R.string.copy_result)
                            val snackDesc = stringResource(R.string.result_copied)
                            IconButton(
                                onClick = {
                                    feedbackManager.provideFeedback(view)
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Calculator Result",
                                                    viewModel.formatNumber(
                                                        uiState.input,
                                                        shortMode = false,
                                                        inputLine = false
                                                    )
                                                )
                                            )
                                        )
                                        snackbarHostState.showSnackbar(snackDesc, withDismissAction = true)
                                    }
                                },
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = desc)
                            }
                        }
                    }
                    if (!keyboardVisible) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    feedbackManager.provideFeedback(view)
                                    keyboardVisible = true
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Keyboard area
            if (keyboardVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyboardHeight)
                ) {
                    if (uiState.mode == CalculatorMode.STANDARD) {
                        StandardKeyboard(
                            onInput = viewModel::onInput,
                            onClear = viewModel::clear,
                            onBack = viewModel::backspace,
                            onEquals = viewModel::calculate,
                            onToggleMode = viewModel::toggleMode
                        )
                    } else {
                        ScientificKeyboard(
                            inverse = uiState.inverse,
                            onInput = viewModel::onInput,
                            onClear = viewModel::clear,
                            onBack = viewModel::backspace,
                            onEquals = viewModel::calculate,
                            onToggleMode = viewModel::toggleMode,
                            onToggleInverse = viewModel::toggleInverse,
                            onToggleDegreeMode = viewModel::toggleDegreeMode,
                            isDegreeMode = uiState.isDegree
                        )
                    }
                }
            }
        }
    }
}

private class CalculationAdapter(
    private val viewModel: CalculatorViewModel,
    private val textColor: Int,
    private val resultColor: Int
) : RecyclerView.Adapter<CalculationViewHolder>() {
    private var items: List<CalculationEntity> = emptyList()

    fun updateData(newItems: List<CalculationEntity>) {
        val diffCallback = CalculationDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalculationViewHolder {
        val ctx = parent.context
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 12, 16, 12)
        }
        val inputTv = TextView(ctx).apply {
            textSize = 20f
            setTextColor(textColor)
        }
        val resultTv = TextView(ctx).apply {
            textSize = 20f
            setTextColor(resultColor)
        }
        container.addView(inputTv)
        container.addView(resultTv)
        return CalculationViewHolder(container, inputTv, resultTv)
    }

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: CalculationViewHolder, position: Int) {
        val entry = items[position]
        holder.inputView.text = viewModel.formatNumber(entry.input, shortMode = false, inputLine = false)
        holder.resultView.text = "= ${viewModel.formatNumber(entry.result, shortMode = false, inputLine = false)}"
    }
}

private class CalculationViewHolder(
    view: android.view.View,
    val inputView: TextView,
    val resultView: TextView
) : RecyclerView.ViewHolder(view)

class CalculationDiffCallback(
    private val oldList: List<CalculationEntity>,
    private val newList: List<CalculationEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}