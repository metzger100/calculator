package com.metzger100.calculator.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.metzger100.calculator.util.FeedbackManager

@Composable
fun BottomNavBar(navController: NavController) {
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val view = LocalView.current

    val items = listOf(NavItem.Calculator, NavItem.Currency, NavItem.Units)
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val labelText = stringResource(id = item.labelRes)

            val isSelected = when {
                currentRoute == item.route -> true
                item == NavItem.Units && currentRoute?.startsWith("unit/") == true -> true
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    feedbackManager.provideFeedback(view)
                    if (currentRoute != item.route) {
                        /* 🔑  Pop “settings” if it’s anywhere in the stack  */
                        navController.popBackStack("settings", inclusive = true)

                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true                 // keep state of other tabs
                            }
                            launchSingleTop = true
                            restoreState   = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = labelText) },
                label = { Text(labelText) }
            )
        }
    }
}