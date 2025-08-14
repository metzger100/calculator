package com.metzger100.calculator.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.metzger100.calculator.features.calculator.ui.CalculatorScreen
import com.metzger100.calculator.features.calculator.viewmodel.CalculatorViewModel
import com.metzger100.calculator.features.currency.ui.CurrencyConverterScreen
import com.metzger100.calculator.features.currency.viewmodel.CurrencyViewModel
import com.metzger100.calculator.features.settings.ui.SettingsScreen
import com.metzger100.calculator.features.settings.viewmodel.SettingsViewModel
import com.metzger100.calculator.features.unit.ui.UnitConverterOverviewScreen
import com.metzger100.calculator.features.unit.ui.UnitConverterScreen
import com.metzger100.calculator.features.unit.viewmodel.UnitConverterViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavGraph(
    navController: NavHostController,
    calculatorViewModel: CalculatorViewModel,
    currencyViewModel: CurrencyViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    openKeyboardOnStart: Boolean,
    scientificOnStart: Boolean,
    startDestination: String? = "calculator"
) {
    NavHost(navController, startDestination = startDestination ?: "calculator") {
        composable(NavItem.Calculator.route) {
            CalculatorScreen(
                viewModel = calculatorViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = scope,
                openKeyboardOnStart = openKeyboardOnStart,
                scientificOnStart = scientificOnStart
            )
        }
        composable("calculator_scientific") {
            CalculatorScreen(
                viewModel = calculatorViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = scope,
                openKeyboardOnStart = openKeyboardOnStart,
                scientificOnStart = true
            )
        }
        composable(NavItem.Currency.route) {
            CurrencyConverterScreen(
                viewModel = currencyViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = scope
            )
        }
        composable(NavItem.Units.route) {
            UnitConverterOverviewScreen(navController)
        }
        composable(
            route = "unit/{category}",
            arguments = listOf(navArgument("category") {
                type = NavType.StringType
            })
        ) {
            val unitViewModel: UnitConverterViewModel = hiltViewModel()
            UnitConverterScreen(
                viewModel = unitViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = scope
            )
        }
        composable("settings") {
            val vm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = vm
            )
        }
    }
}