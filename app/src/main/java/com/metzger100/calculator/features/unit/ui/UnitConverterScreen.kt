// com.metzger100.calculator.features.unit.ui.UnitConverterScreen.kt
package com.metzger100.calculator.features.unit.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.UnitHistoryEntity
import com.metzger100.calculator.features.unit.viewmodel.UnitConverterViewModel
import com.metzger100.calculator.features.unit.ui.UnitConverterConstants.UnitDef
import com.metzger100.calculator.util.FeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val uiState by viewModel::uiState
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current
    val context = LocalContext.current

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val keyboardHeight = maxHeight * 0.5f

        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = keyboardHeight)
        ) {
            UnitRow(
                labelRes       = uiState.fromUnit.nameRes,
                value          = uiState.fromValue,
                isSel          = (uiState.selectedField == 1),
                units          = viewModel.availableUnits,
                onUnitSelected = viewModel::onFromUnitChanged,
                onClick        = { viewModel.onSelectField(1) },
                formatNumber   = { str, short -> viewModel.formatNumber(str, short) },
                snackbarHostState = snackbarHostState,
                coroutineScope = coroutineScope,
                feedbackManager = feedbackManager,
                view = view
            )
            Spacer(Modifier.height(8.dp))
            UnitRow(
                labelRes       = uiState.toUnit.nameRes,
                value          = uiState.toValue,
                isSel          = (uiState.selectedField == 2),
                units          = viewModel.availableUnits,
                onUnitSelected = viewModel::onToUnitChanged,
                onClick        = { viewModel.onSelectField(2) },
                formatNumber   = { str, short -> viewModel.formatNumber(str, short) },
                snackbarHostState = snackbarHostState,
                coroutineScope = coroutineScope,
                feedbackManager = feedbackManager,
                view = view
            )
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                val ctx = LocalContext.current
                val textColor   = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                val resultColor = MaterialTheme.colorScheme.primary.toArgb()

                AndroidView(
                    factory = {
                        RecyclerView(ctx).apply {
                            layoutManager =
                                LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true)
                            adapter = UnitHistoryAdapter(
                                format = { s, short -> viewModel.formatNumber(s, short) },
                                textColor = textColor,
                                resultColor = resultColor
                            )
                            clipToPadding = true
                            clipChildren = true
                        }
                    },
                    update = { rv ->
                        val adapter = rv.adapter as UnitHistoryAdapter
                        adapter.updateData(viewModel.unitHistory)
                        if (viewModel.unitHistory.isNotEmpty()) rv.scrollToPosition(0)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Keyboard (with "=" that writes to history + clears)
        Box(
            Modifier
                .fillMaxWidth()
                .height(keyboardHeight)
                .align(Alignment.BottomCenter)
        ) {
            UnitConverterKeyboard(
                onInput = { label ->
                    val current = if (uiState.selectedField == 1) uiState.fromValue else uiState.toValue
                    viewModel.onValueChange(current + label)
                },
                onClear = { viewModel.onValueChange("") },
                onBack = {
                    val current = if (uiState.selectedField == 1) uiState.fromValue else uiState.toValue
                    if (current.isNotEmpty()) viewModel.onValueChange(current.dropLast(1))
                },
                onEquals = {
                    val (fromV, fromU, toV, toU) =
                        if (uiState.selectedField == 1)
                            arrayOf(uiState.fromValue,
                                context.getString(uiState.fromUnit.nameRes),
                                uiState.toValue,
                                context.getString(uiState.toUnit.nameRes))
                        else
                            arrayOf(uiState.toValue,
                                context.getString(uiState.toUnit.nameRes),
                                uiState.fromValue,
                                context.getString(uiState.fromUnit.nameRes))

                    if (fromV.isNotBlank() && toV.isNotBlank() && fromV != "0" && toV != "0") {
                        coroutineScope.launch {
                            viewModel.addToHistory(
                                fromValue = fromV,
                                fromUnit  = fromU,
                                toValue   = toV,
                                toUnit    = toU
                            )
                        }
                    }
                    viewModel.onValueChange("")
                }
            )
        }
    }
}

@Composable
fun UnitRow(
    @StringRes labelRes: Int,
    value: String,
    isSel: Boolean,
    units: List<UnitDef>,
    onUnitSelected: (UnitDef) -> Unit,
    onClick: () -> Unit,
    formatNumber: (String, Boolean) -> String,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    feedbackManager: FeedbackManager,
    view: View
) {
    var show by remember { mutableStateOf(false) }
    val borderM = if (isSel) Modifier.border(
        2.dp,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.shapes.medium
    ) else Modifier

    val clipboard = LocalClipboard.current

    Card(
        Modifier
            .fillMaxWidth()
            .then(borderM)
            .clickable {
                feedbackManager.provideFeedback(view)
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val unitLabel = stringResource(labelRes)
            val changeUnitDesc = stringResource(R.string.change_unit_content_description, unitLabel)
            Text(
                text = stringResource(labelRes),
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        feedbackManager.provideFeedback(view)
                        show = true
                    }
                    .semantics {
                        contentDescription = changeUnitDesc
                    }
            )
            Spacer(Modifier.width(16.dp))

            val displayText = if (isSel) {
                value.ifEmpty { "0" }
            } else {
                formatNumber(value, false)
            }

            Text(
                text = displayText,
                fontSize = if (isSel) 24.sp else 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                softWrap = true,
                maxLines = Int.MAX_VALUE
            )

            if (value.isNotEmpty() && value != "0") {
                val snackDesc = stringResource(R.string.value_copied)
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            feedbackManager.provideFeedback(view)
                            clipboard.setClipEntry(
                                ClipEntry(ClipData.newPlainText("Unit Value", value))
                            )
                            snackbarHostState.showSnackbar(
                                message = snackDesc,
                                withDismissAction = true
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_value)
                    )
                }
            }
        }
    }

    if (show) {
        UnitSelectorDialogRV(
            units = units,
            onUnitSelected = {
                onUnitSelected(it)
                show = false
            },
            onDismissRequest = { show = false },
            feedbackManager = feedbackManager
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun UnitSelectorDialogRV(
    units: List<UnitDef>,
    onUnitSelected: (UnitDef) -> Unit,
    onDismissRequest: () -> Unit,
    feedbackManager: FeedbackManager
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints {
            val maxDialogHeight = maxHeight * 0.75f

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = maxDialogHeight)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        RecyclerView(context).apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = object : RecyclerView.Adapter<UnitViewHolder>() {
                                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
                                    val textView = TextView(context).apply {
                                        setPadding(32, 24, 32, 24)
                                        textSize = 18f
                                        setTextColor(textColor)
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                    }
                                    return UnitViewHolder(textView)
                                }
                                override fun getItemCount() = units.size
                                override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
                                    val unit = units[position]
                                    val unitName = holder.textView.context.getString(unit.nameRes)
                                    holder.textView.text = unitName

                                    holder.textView.contentDescription = holder.textView.context.getString(
                                        R.string.select_unit_content_description, unitName
                                    )

                                    holder.itemView.setOnClickListener { v ->
                                        feedbackManager.provideFeedback(v, sound = false)
                                        onUnitSelected(unit)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

private class UnitViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

// ───────────────────────── Embedded adapter (like Currency) ─────────────────────────

private class UnitHistoryAdapter(
    private val format: (String, Boolean) -> String,  // viewModel::formatNumber
    private val textColor: Int,
    private val resultColor: Int
) : RecyclerView.Adapter<UnitHistoryViewHolder>() {

    private var items: List<UnitHistoryEntity> = emptyList()

    fun updateData(newItems: List<UnitHistoryEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(o: Int, n: Int) = items[o].id == newItems[n].id
            override fun areContentsTheSame(o: Int, n: Int) = items[o] == newItems[n]
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitHistoryViewHolder {
        val ctx = parent.context
        val ll = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val timeTv = TextView(ctx).apply {
            textSize = 12f
            setTextColor(textColor)                 // timestamp = onSurfaceVariant
        }
        val lineTv = TextView(ctx).apply {
            textSize = 16f
            setTextColor(resultColor)               // value line = primary (blue)
        }
        ll.addView(timeTv)
        ll.addView(lineTv)
        return UnitHistoryViewHolder(ll, timeTv, lineTv)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: UnitHistoryViewHolder, position: Int) {
        val e = items[position]

        // timestamp
        holder.timeTv.text = Instant.ofEpochMilli(e.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        // short-mode rule: derive from *input* (fromValue)
        val shortMode = runCatching {
            BigDecimal(e.fromValue).stripTrailingZeros().scale() <= 2
        }.getOrDefault(true)

        val fromFmt = format(e.fromValue, shortMode)
        val toFmt   = format(e.toValue,   shortMode)

        holder.lineTv.text = "${e.fromUnit}: $fromFmt  →  ${e.toUnit}: $toFmt"
    }
}

private class UnitHistoryViewHolder(
    view: View,
    val timeTv: TextView,
    val lineTv: TextView
) : RecyclerView.ViewHolder(view)
