package com.metzger100.calculator.features.unit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metzger100.calculator.R
import com.metzger100.calculator.util.FeedbackManager

/**
 * Einzelne Kategorie für die Übersicht
 */
data class UnitCategory(
    val route: String,
    val displayName: String,
    val icon: ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitConverterOverviewScreen(
    navController: NavController
) {
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current
    val categories = listOf(
        UnitCategory(
            "Length",
            stringResource(R.string.UnitConvCat_Length),
            Icons.Default.Straighten
        ),
        UnitCategory(
            "Weight",
            stringResource(R.string.UnitConvCat_Weight),
            Icons.Default.FitnessCenter
        ),
        UnitCategory(
            "Volume",
            stringResource(R.string.UnitConvCat_Volume),
            Icons.Default.LocalDrink
        ),
        UnitCategory("Area", stringResource(R.string.UnitConvCat_Area), Icons.Default.CropSquare),
        UnitCategory(
            "Temperature",
            stringResource(R.string.UnitConvCat_Temperature),
            Icons.Default.Thermostat
        ),
        UnitCategory("Time", stringResource(R.string.UnitConvCat_Time), Icons.Default.AccessTime),
        UnitCategory("Speed", stringResource(R.string.UnitConvCat_Speed), Icons.Default.Speed),
        UnitCategory("Energy", stringResource(R.string.UnitConvCat_Energy), Icons.Default.FlashOn),
        UnitCategory(
            "Power",
            stringResource(R.string.UnitConvCat_Power),
            Icons.Default.PowerSettingsNew
        ),
        UnitCategory(
            "Pressure",
            stringResource(R.string.UnitConvCat_Pressure),
            Icons.Default.Opacity
        ),
        UnitCategory(
            "Frequency",
            stringResource(R.string.UnitConvCat_Frequency),
            Icons.Default.Tune
        ),
        UnitCategory("Data", stringResource(R.string.UnitConvCat_Data), Icons.Default.Storage),
        UnitCategory(
            "FuelEconomy",
            stringResource(R.string.UnitConvCat_FuelEconomy),
            Icons.Default.LocalGasStation
        ),
        UnitCategory(
            "PlaneAngle",
            stringResource(R.string.UnitConvCat_PlaneAngle),
            Icons.AutoMirrored.Filled.Undo
        ),
        UnitCategory("Amount", stringResource(R.string.UnitConvCat_Amount), Icons.Default.Science)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(categories) { category ->
            CategoryCard(category) {
                feedbackManager.provideFeedback(view)
                navController.navigate("unit/${category.route}")
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: UnitCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
