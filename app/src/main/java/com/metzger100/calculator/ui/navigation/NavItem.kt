package com.metzger100.calculator.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.ui.graphics.vector.ImageVector
import com.metzger100.calculator.R

// 1) NavItem speichert nur noch die @StringRes-ID
sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    @field:StringRes val labelRes: Int
) {
    object Calculator : NavItem("calculator", Icons.Filled.Calculate, R.string.app_name)
    object Currency : NavItem("currency", Icons.Filled.CurrencyExchange, R.string.NavItem_Currency)
    object Units : NavItem("units", Icons.Filled.Straighten, R.string.NavItem_Units)
}
