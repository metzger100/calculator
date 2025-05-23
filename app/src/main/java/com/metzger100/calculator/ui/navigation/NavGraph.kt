package com.metzger100.calculator.ui.navigation

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
import com.metzger100.calculator.features.unit.ui.UnitConverterOverviewScreen
import com.metzger100.calculator.features.unit.ui.UnitConverterScreen
import com.metzger100.calculator.features.unit.viewmodel.UnitConverterViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    calculatorViewModel: CalculatorViewModel,
    currencyViewModel: CurrencyViewModel
) {
    NavHost(navController, startDestination = NavItem.Calculator.route) {
        composable(NavItem.Calculator.route) {
            CalculatorScreen(viewModel = calculatorViewModel)
        }
        composable(NavItem.Currency.route) {
            CurrencyConverterScreen(viewModel = currencyViewModel)
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
                viewModel = unitViewModel
            )
        }
    }
}