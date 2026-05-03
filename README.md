<!--suppress LongLine -->

# Material You Calculator

![Calculator Logo](svg/ic_logo.svg)

An elegant, Android-native calculator and currency converter app, built with **Jetpack Compose** and
styled using **Material You** (Monet) theming. The app adapts dynamically to your system colors and
offers a clean, modern experience for daily calculations and conversions.

---

## ✨ Features

- 🧮 **Basic Calculator**  
  Perform quick and simple arithmetic operations with a responsive interface.

- 💱 **Currency Converter**  
  Converts between currencies using real-time exchange rates (data fetched from a free public API).

  ⚠️ Note:  
  Exchange rates are updated only once per day (02:00 UTC).  
  All conversions are based on USD as an intermediate currency, which may cause minor rounding
  differences for certain pairs.

- 🧳 **Unit Converter Tab**  
  Convert between various units such as length, weight, temperature, and more with optimized keypads
  and a clear interface.

- 🎨 **Material You Support**  
  Adapts to your system’s dynamic color palette (Monet), including light/dark themes and accent
  coloring.

- 💾 **Calculation History**  
  View and clear previous operations in a neat list, persisted across app restarts via Room.

- ☁️ **Offline Caching**  
  Exchange rates and history are cached locally (Room/DataStore) for offline use — fresh data is
  fetched once a day.

- ⌨️ **Custom Keypads**  
  Separate numeric/math keypad for the calculator and currency converter for optimized input.

- 📱 **Edge-to-Edge UI**  
  Seamlessly blends with Android's status and gesture navigation bars using the `EdgeToEdge` API.

---

## 🚧 To-Do

- ✅ **Verification**  
  Ensure functionality of all features and the accuracy of conversions and calculations.

---

## 🛠️ Tech Stack

- **Kotlin**
- **Jetpack Compose** for UI
- **Hilt** for Dependency Injection
- **MVVM** architecture
- **Room** for local database
- **Ktor** HTTP client
- **Gson** for JSON parsing
- **Material Design 3 (Material You)**

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Electric/Eiger or newer
- Android SDK 33+
- Kotlin 1.9+

### Clone the repo

```bash
git clone https://github.com/metzger100/calculator.git
cd calculator
```

### Run the app

Open the project in Android Studio, let Gradle sync, then click **Run**.

---

## 🗂️ Project Structure

```
com.metzger100.calculator
├── data                        # Data and Repository layer
│   ├── local                   # Room entities & DAOs
│   │   ├── dao                 # Contains all DAOs
│   │   │   ├── CalculationDao.kt
│   │   │   ├── CurrencyHistoryDao.kt
│   │   │   ├── CurrencyListDao.kt
│   │   │   ├── CurrencyPrefsDao.kt
│   │   │   └── CurrencyRateDao.kt
│   │   ├── entity              # Room entities
│   │   │   ├── CalculationEntity.kt
│   │   │   ├── CurrencyHistoryEntity.kt
│   │   │   ├── CurrencyListEntity.kt
│   │   │   ├── CurrencyPrefsEntity.kt
│   │   │   └── CurrencyRateEntity.kt
│   │   └── database            # Room database & DAO interfaces
│   │       └── CalculatorDatabase.kt
│   ├── repository              # Repository classes handling data sources
│   │   ├── CalculatorRepository.kt
│   │   ├── CurrencyHistoryRepository.kt
│   │   └── CurrencyRepository.kt
│   └── ConnectivityObserver.kt
│
├── di                          # Dependency Injection (Hilt) - Modules & Qualifiers
│   ├── modules                 # DI modules
│   │   └── AppModule.kt        # Provides Room, HttpClient, Dispatchers
│   └── Qualifiers.kt           # Custom qualifiers for Hilt DI (if needed)
│
├── features                    # Features for different functionalities
│   ├── calculator              # Calculator feature
│   │   ├── model               # Model data (e.g. calculator modes)
│   │   │   └── CalculatorMode.kt
│   │   ├── ui                  # UI components and Composables for the Calculator
│   │   │   ├── CalculatorScreen.kt
│   │   │   ├── KeyboardButton.kt
│   │   │   ├── StandardKeyboard.kt
│   │   │   └── ScientificKeyboard.kt
│   │   └── viewmodel           # ViewModel for the Calculator feature
│   │       └── CalculatorViewModel.kt
│   ├── currency                # Currency Converter feature
│   │   ├── ui                  # UI components for the Currency Converter
│   │   │   ├── CurrencyConverterScreen.kt
│   │   │   ├── CurrencyConverterKeyboard.kt
│   │   │   └── CurrencyConverterConstants.kt
│   │   │      # Constants for available currencies in the converter UI
│   │   └── viewmodel           # ViewModel for the Currency Converter
│   │       └── CurrencyViewModel.kt
│   ├── unit                    # Unit Converter feature
│   │   ├── ui                  # UI components for Unit Converter
│   │   │   ├── UnitConverterOverviewScreen.kt
│   │   │   ├── UnitConverterScreen.kt
│   │   │   ├── UnitConverterKeyboard.kt
│   │   │   └── UnitConverterConstants.kt  # Constants for Unit Converter UI
│   │   └── viewmodel           # ViewModel for the Unit Converter feature
│   │       └── UnitConverterViewModel.kt
│   └── settings                               # ➊ NEW
│       ├── ui
│       │   └── SettingsScreen.kt
│       └── viewmodel
│           └── SettingsViewModel.kt
│
├── ui                          # UI components and Navigation setup
│   ├── navigation              # Navigation (NavGraph, BottomNavBar, etc.)
│   │   ├── BottomNavBar.kt     # Bottom Navigation Bar
│   │   ├── NavGraph.kt         # Navigation graph for the app
│   │   ├── NavItem.kt          # Items for Bottom Navigation
│   │   └── TopAppBar.kt        # Top AppBar for screens
│   └── theme                   # Material You Theme (colors, typography, etc.)
│       ├── Theme.kt            # Application-wide theme
│       └── Type.kt             # Font definitions and typography setup
│
├── util
│   ├── FeedbackManager.kt      # Util Class für haptic Feedback
│   └── format
│       ├── NumberFormatModule.kt  # Module for the service class
│       └── NumberFormatService.kt # Service for readable numbers
│
└── MainActivity.kt             # Entry point of the app (MainActivity)
```

---

## 📄 License

GNU AFFERO GENERAL PUBLIC LICENSE Version 3

---

## 🙌 Credits

- Exchange rate data provided
  by [fawazahmed0/exchange-api](https://github.com/fawazahmed0/exchange-api)

Feel free to contribute by submitting issues or pull requests!
