// com.metzger100.calculator.features.currency.ui.CurrencyConverterScreen.kt
package com.metzger100.calculator.features.currency.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metzger100.calculator.R
import com.metzger100.calculator.data.local.entity.CurrencyHistoryEntity
import com.metzger100.calculator.features.currency.viewmodel.CurrencyViewModel
import com.metzger100.calculator.features.currency.ui.CurrencyConverterConstants.MajorCurrencyCodes
import com.metzger100.calculator.util.FeedbackManager
import com.metzger100.calculator.util.format.NumberFormatService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CurrencyConverterScreen(
    viewModel: CurrencyViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    // gesamter UI‑State
    val uiState by viewModel::uiState
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current

    // StateFlow → collectAsState()
    val currenciesWithTitles by viewModel.currenciesWithTitles.collectAsState()
    val rates                   by viewModel.rates.collectAsState()
    val lastApiDate             by viewModel.lastApiDate.collectAsState()

    // nur wichtige Währungen
    val filtered by remember(currenciesWithTitles) {
        derivedStateOf {
            currenciesWithTitles
                .filter { (code, _) -> MajorCurrencyCodes.contains(code) }
                .sortedBy { MajorCurrencyCodes.indexOf(it.first) }
        }
    }
    val codeList = when {
        filtered.isNotEmpty()       -> filtered
        currenciesWithTitles.isNotEmpty() -> currenciesWithTitles
        else                        -> listOf("USD" to "US Dollar", "EUR" to "Euro")
    }

    // short‑Mode Erkennung
    val shortInput1 = runCatching {
        BigDecimal(uiState.value1.takeIf { it.isNotBlank() } ?: "0")
            .stripTrailingZeros().scale() < 3
    }.getOrDefault(true)
    val shortInput2 = runCatching {
        BigDecimal(uiState.value2.takeIf { it.isNotBlank() } ?: "0")
            .stripTrailingZeros().scale() < 3
    }.getOrDefault(true)

    // onResume → refresh
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val keyboardHeight = maxHeight * 0.5f
        val infoHeight = 48.dp

        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = keyboardHeight + infoHeight)
        ) {
            if (rates.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_exchange_data),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .padding(12.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            CurrencyRow(
                currency    = codeList.find { it.first == uiState.currency1 }?.second ?: uiState.currency1,
                value       = if (uiState.selectedField == 1) uiState.value1 else viewModel.formatNumber(uiState.value1, shortInput2),
                isSelected  = uiState.selectedField == 1,
                currencies  = codeList,
                onCurrencySelected = { viewModel.onCurrencyChanged1(it) },
                onClick     = { viewModel.onSelectField(1) },
                snackbarHostState = snackbarHostState,
                coroutineScope    = coroutineScope,
                feedbackManager   = feedbackManager,
                view              = view
            )
            Spacer(Modifier.height(8.dp))
            CurrencyRow(
                currency    = codeList.find { it.first == uiState.currency2 }?.second ?: uiState.currency2,
                value       = if (uiState.selectedField == 2) uiState.value2 else viewModel.formatNumber(uiState.value2, shortInput1),
                isSelected  = uiState.selectedField == 2,
                currencies  = codeList,
                onCurrencySelected = { viewModel.onCurrencyChanged2(it) },
                onClick     = { viewModel.onSelectField(2) },
                snackbarHostState = snackbarHostState,
                coroutineScope    = coroutineScope,
                feedbackManager   = feedbackManager,
                view              = view
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                val context = LocalContext.current
                val textColor   = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                val resultColor = MaterialTheme.colorScheme.primary.toArgb()

                AndroidView(
                    factory = {
                        RecyclerView(context).apply {
                            layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                            adapter = CurrencyHistoryAdapter(viewModel.numberFormatService, textColor, resultColor)
                            clipToPadding = true
                            clipChildren = true
                        }
                    },
                    update = { rv ->
                        val adapter = rv.adapter as CurrencyHistoryAdapter
                        adapter.updateData(viewModel.currencyHistory)     // newest list
                        if (viewModel.currencyHistory.isNotEmpty()) rv.scrollToPosition(0)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        ExchangeRateInfo(
            lastApiDate = lastApiDate,
            modifier    = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = keyboardHeight)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(keyboardHeight)
                .align(Alignment.BottomCenter)
        ) {
            CurrencyConverterKeyboard(
                onInput = { label ->
                    val current = if (uiState.selectedField == 1) uiState.value1 else uiState.value2
                    viewModel.onValueChange(current + label)
                },
                onClear = { viewModel.onValueChange("") },
                onBack = {
                    val current = if (uiState.selectedField == 1) uiState.value1 else uiState.value2
                    if (current.isNotEmpty()) viewModel.onValueChange(current.dropLast(1))
                },
                onEquals = {
                    // Only log to history if both values are filled and not zero
                    val amountFrom: String
                    val currencyFrom: String
                    val amountTo: String
                    val currencyTo: String
                    if (uiState.selectedField == 1) {
                        amountFrom = uiState.value1
                        currencyFrom = uiState.currency1
                        amountTo = uiState.value2
                        currencyTo = uiState.currency2
                    } else {
                        amountFrom = uiState.value2
                        currencyFrom = uiState.currency2
                        amountTo = uiState.value1
                        currencyTo = uiState.currency1
                    }
                    if (amountFrom.isNotBlank() && amountTo.isNotBlank() && amountFrom != "0" && amountTo != "0") {
                        coroutineScope.launch {
                            viewModel.addToHistory(amountFrom, currencyFrom, amountTo, currencyTo)
                        }
                    }
                    viewModel.onValueChange("")
                }
            )
        }
    }
}

@Composable
fun ExchangeRateInfo(
    lastApiDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    val nowUtc by produceState(initialValue = Instant.now()) {
        while (true) {
            value = Instant.now()
            delay(60_000L)
        }
    }

    val nowUtcOffset = nowUtc.atOffset(ZoneOffset.UTC)
    val todayUtc     = nowUtcOffset.toLocalDate()
    val threshold    = todayUtc.atTime(2,0).atOffset(ZoneOffset.UTC).toInstant()

    Column(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lastApiDate == null) {
            Text(
                text = stringResource(R.string.no_data_available),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = stringResource(R.string.exchange_rates_as_of) + " " +
                        lastApiDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(4.dp))
        if (lastApiDate == null || (nowUtc >= threshold && lastApiDate.isBefore(todayUtc))) {
            Text(
                text = stringResource(R.string.update_due),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            val nextDateUtc = if (nowUtcOffset.hour < 2) todayUtc else todayUtc.plusDays(1)
            val nextUtcInst = nextDateUtc.atTime(2,0).atOffset(ZoneOffset.UTC).toInstant()
            val nextLocal  = nextUtcInst.atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            Text(
                text = stringResource(R.string.next_rates_update) + " " + nextLocal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CurrencyRow(
    currency: String,
    value: String,
    isSelected: Boolean,
    currencies: List<Pair<String, String>>,
    onCurrencySelected: (String) -> Unit,
    onClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    feedbackManager: FeedbackManager,
    view: View
) {
    var showDialog by remember { mutableStateOf(false) }
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
    } else Modifier

    val clipboard = LocalClipboard.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
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
            val changeCurrencyDesc = stringResource(R.string.change_currency_content_description, currency)
            Text(
                text = currency,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable {
                        feedbackManager.provideFeedback(view)
                        showDialog = true
                    }
                    .semantics {
                        contentDescription = changeCurrencyDesc
                    }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = value.ifEmpty { "0" },
                modifier = Modifier.weight(1f),
                softWrap = true,
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 3.sp,
                    fontSize = if (isSelected)
                        MaterialTheme.typography.headlineLarge.fontSize
                    else
                        20.sp
                )
            )
            if (value.isNotEmpty() && value != "0") {
                val snackDesc = stringResource(R.string.value_copied)
                IconButton(
                    onClick = {
                        feedbackManager.provideFeedback(view)
                        coroutineScope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText("Currency Value", value)
                                )
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

    if (showDialog) {
        CurrencySelectorDialogRV(
            currencies = currencies,
            onCurrencySelected = {
                onCurrencySelected(it)
                showDialog = false
            },
            onDismissRequest = { showDialog = false },
            feedbackManager = feedbackManager
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CurrencySelectorDialogRV(
    currencies: List<Pair<String, String>>,
    onCurrencySelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    feedbackManager: FeedbackManager
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val dialogHeight = remember { mutableStateOf(0.dp) }

    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints {
            val maxDialogHeight = maxHeight * 0.75f
            if (dialogHeight.value == 0.dp) {
                dialogHeight.value = maxDialogHeight
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(dialogHeight.value)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        RecyclerView(context).apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = object : RecyclerView.Adapter<CurrencyViewHolder>() {
                                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
                                    val linearLayout = LinearLayout(context).apply {
                                        orientation = LinearLayout.HORIZONTAL
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                    }
                                    val textView = TextView(context).apply {
                                        setPadding(32,24,32,24)
                                        textSize = 16f
                                        setTextColor(textColor.toArgb())
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                    }
                                    linearLayout.addView(textView)
                                    return CurrencyViewHolder(linearLayout)
                                }
                                override fun getItemCount() = currencies.size
                                override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
                                    val (code, title) = currencies[position]
                                    val linearLayout = holder.itemView as LinearLayout
                                    val textView = linearLayout.getChildAt(0) as TextView
                                    textView.text = title

                                    textView.contentDescription = textView.context.getString(
                                        R.string.select_currency_content_description, title
                                    )

                                    holder.itemView.setOnClickListener { v ->
                                        feedbackManager.provideFeedback(v, sound = false)
                                        onCurrencySelected(code)
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

private class CurrencyHistoryAdapter(
    private val nfs: NumberFormatService,
    private val textColor: Int,
    private val resultColor: Int
) : RecyclerView.Adapter<CurrencyHistoryViewHolder>() {

    private var items: List<CurrencyHistoryEntity> = emptyList()

    fun updateData(newItems: List<CurrencyHistoryEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(o: Int, n: Int) = items[o].id == newItems[n].id
            override fun areContentsTheSame(o: Int, n: Int) = items[o] == newItems[n]
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHistoryViewHolder {
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
            setTextColor(textColor)
        }
        val lineTv = TextView(ctx).apply {
            textSize = 16f
            setTextColor(textColor)
        }
        ll.addView(timeTv)
        ll.addView(lineTv)
        return CurrencyHistoryViewHolder(ll, timeTv, lineTv)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CurrencyHistoryViewHolder, position: Int) {
        val e = items[position]

        // ─ timestamp – unchanged
        val ts = Instant.ofEpochMilli(e.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        holder.timeTv.setTextColor(textColor)
        holder.timeTv.text = ts

        // ─ decide “short mode” from the *input* (amountFrom)
        val shortMode = runCatching {
            BigDecimal(e.amountFrom).stripTrailingZeros().scale() <= 2
        }.getOrDefault(true)

        val fromFmt = nfs.formatNumber(e.amountFrom, shortMode, inputLine = false)
        val toFmt   = nfs.formatNumber(e.amountTo  , shortMode, inputLine = false)

        holder.lineTv.setTextColor(resultColor)          // same blue as calculator
        holder.lineTv.text = "${e.currencyFrom}: $fromFmt  →  " +
                "${e.currencyTo}: $toFmt"
    }
}

private class CurrencyHistoryViewHolder(
    view: View,
    val timeTv: TextView,
    val lineTv: TextView
) : RecyclerView.ViewHolder(view)

private class CurrencyViewHolder(view: View) : RecyclerView.ViewHolder(view)