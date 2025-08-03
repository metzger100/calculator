package com.metzger100.calculator.features.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metzger100.calculator.R
import com.metzger100.calculator.features.settings.viewmodel.SettingsViewModel
import com.metzger100.calculator.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val mode by viewModel.themeMode.collectAsState()
    val openKeyboard by viewModel.openKeyboardOnStart.collectAsState()
    val scientific by viewModel.scientificOnStart.collectAsState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text   = stringResource(R.string.Settings_ThemeTitle),
            style  = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            ThemeMode.entries.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (mode == option),
                            onClick  = { viewModel.onThemeSelected(option) }
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (option) {
                            ThemeMode.SYSTEM -> stringResource(R.string.Settings_ThemeSystem)
                            ThemeMode.LIGHT  -> stringResource(R.string.Settings_ThemeLight)
                            ThemeMode.DARK   -> stringResource(R.string.Settings_ThemeDark)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    RadioButton(
                        selected = (mode == option),
                        onClick  = null
                    )
                }
                if (index < ThemeMode.entries.toTypedArray().lastIndex) {
                    HorizontalDivider(thickness = 0.6.dp, color = DividerDefaults.color)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text   = stringResource(R.string.Settings_BehaviorOptions),
            style  = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                SettingSwitchRow(
                    checked = openKeyboard,
                    title = stringResource(R.string.Settings_OpenKeyboard),
                    onCheckedChange = viewModel::onOpenKeyboardOnStartChange
                )
                HorizontalDivider(thickness = 0.6.dp, color = DividerDefaults.color)
                SettingSwitchRow(
                    checked = scientific,
                    title = stringResource(R.string.Settings_ScientificDefault),
                    onCheckedChange = viewModel::onScientificOnStartChange
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    checked: Boolean,
    title: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
