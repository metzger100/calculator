package com.metzger100.calculator

import TopAppBar
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.metzger100.calculator.ui.navigation.BottomNavBar
import com.metzger100.calculator.ui.navigation.NavGraph
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.metzger100.calculator.features.calculator.viewmodel.CalculatorViewModel
import com.metzger100.calculator.features.currency.viewmodel.CurrencyViewModel
import com.metzger100.calculator.ui.theme.CalculatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@HiltAndroidApp
class MainApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    CalculatorTheme {
        val navController = rememberNavController()
        val CalcViewModel: CalculatorViewModel = hiltViewModel()
        val CurViewModel: CurrencyViewModel = hiltViewModel()

        val navBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStack?.destination?.route

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val localZone = ZoneId.systemDefault()
        val refreshLocalTime = remember {
            LocalDate.now(ZoneOffset.UTC)
                .atTime(LocalTime.MIDNIGHT)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(localZone)
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        val msg = stringResource(
            R.string.Snackbar_CurRefreshData,
            refreshLocalTime
        )
        val acl = stringResource(R.string.Snackbar_CurRefreshData_Dismiss)

        val showBack = currentRoute?.startsWith("unit/") == true

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionColor    = MaterialTheme.colorScheme.primary
                    )
                }
            },
            topBar = {
                TopAppBar(
                    showBackButton = showBack,
                    onBackClick    = { navController.navigateUp() },
                    onClearHistory = CalcViewModel::clearHistory,
                    onRefreshRates = {
                        CurViewModel.refreshData()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = msg,
                                actionLabel = acl
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavBar(navController = navController)
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                NavGraph(
                    navController = navController,
                    calculatorViewModel = CalcViewModel,
                    currencyViewModel = CurViewModel
                )
            }
        }
    }
}