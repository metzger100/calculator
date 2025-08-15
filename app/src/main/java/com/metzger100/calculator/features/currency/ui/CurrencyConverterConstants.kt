// com.metzger100.calculator.features.currency.ui.Constants.kt
package com.metzger100.calculator.features.currency.ui

object CurrencyConverterConstants {
    // Only these currencies will be shown in the dropdown:
    val MajorCurrencyCodes = listOf(
    // Top most traded currencies globally
    "USD", "EUR", "JPY", "GBP", "CHF", "CAD", "AUD", "CNY",
    // Important currencies by region
    "HKD", "SGD", "SEK", "NOK", "DKK", "NZD", "KRW",
    // Emerging economies
    "BRL", "MXN", "INR", "ZAR", "TRY", "RUB",
    // Europe (non-euro)
    "PLN", "CZK", "HUF", "RON", "BGN",
    // Asia
    "THB", "MYR", "PHP", "IDR",
    // Others
    "ILS", "ISK"
)
}