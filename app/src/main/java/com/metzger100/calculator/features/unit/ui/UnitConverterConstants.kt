package com.metzger100.calculator.features.unit.ui

import androidx.annotation.StringRes
import com.metzger100.calculator.R
import java.math.BigDecimal

/**
 * Defines per category a list of units with conversion factor relative to the base unit.
 * Verified factors against authoritative sources and extended with additional units.
 */
object UnitConverterConstants {

    /** Unit definition with name resource and factor to base unit */
    data class UnitDef(
        @StringRes val nameRes: Int,
        val factorToBase: BigDecimal
    )

    /** Map: Category → List of all UnitDef in that category */
    val units: Map<String, List<UnitDef>> = mapOf(
        // LENGTH (grouped by metric, imperial, astronomical) (base = meter)
        "Length" to listOf(
            // Metric & non-SI small
            UnitDef(R.string.UnitConvCatLength_Angstrom, BigDecimal("1e-10")),
            UnitDef(R.string.UnitConvCatLength_Nanometer, BigDecimal("1e-9")),
            UnitDef(R.string.UnitConvCatLength_Micrometer, BigDecimal("1e-6")),
            UnitDef(R.string.UnitConvCatLength_Millimeter, BigDecimal("0.001")),
            UnitDef(R.string.UnitConvCatLength_Centimeter, BigDecimal("0.01")),
            UnitDef(R.string.UnitConvCatLength_Meter, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatLength_Kilometer, BigDecimal("1000.0")),
            // Imperial
            UnitDef(R.string.UnitConvCatLength_Inch, BigDecimal("0.0254")),
            UnitDef(R.string.UnitConvCatLength_Foot, BigDecimal("0.3048")),
            UnitDef(R.string.UnitConvCatLength_Yard, BigDecimal("0.9144")),
            UnitDef(R.string.UnitConvCatLength_Mile, BigDecimal("1609.344")),
            UnitDef(R.string.UnitConvCatLength_NauticalMile, BigDecimal("1852.0")),
            // Astronomical
            UnitDef(R.string.UnitConvCatLength_AstronomicalUnit, BigDecimal("1.495978707e11")),
            UnitDef(R.string.UnitConvCatLength_LightYear, BigDecimal("9.4607304725808e15")),
            UnitDef(R.string.UnitConvCatLength_Parsec, BigDecimal("3.085677581491367e16"))
        ),

        // WEIGHT/MASS (metric then non-SI then imperial) (base = kilogram)
        "Weight" to listOf(
            // Metric
            UnitDef(R.string.UnitConvCatWeight_Milligram, BigDecimal("0.000001")),
            UnitDef(R.string.UnitConvCatWeight_Gram, BigDecimal("0.001")),
            UnitDef(R.string.UnitConvCatWeight_Carat, BigDecimal("0.0002")),
            UnitDef(R.string.UnitConvCatWeight_Kilogram, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatWeight_Tonne, BigDecimal("1000.0")),
            // Imperial & non-SI
            UnitDef(R.string.UnitConvCatWeight_Ounce, BigDecimal("0.028349523125")),
            UnitDef(R.string.UnitConvCatWeight_Pound, BigDecimal("0.45359237")),
            UnitDef(R.string.UnitConvCatWeight_Stone, BigDecimal("6.35029318")),
            UnitDef(R.string.UnitConvCatWeight_ShortHundredweight, BigDecimal("45.359237")),
            UnitDef(R.string.UnitConvCatWeight_LongHundredweight, BigDecimal("50.80234544")),
            UnitDef(R.string.UnitConvCatWeight_US_Ton, BigDecimal("907.18474")),
            UnitDef(R.string.UnitConvCatWeight_LongTon, BigDecimal("1016.0469088"))
        ),

        // VOLUME (metric then US then UK) (base = liter)
        "Volume" to listOf(
            UnitDef(R.string.UnitConvCatVolume_Milliliter, BigDecimal("0.001")),
            UnitDef(R.string.UnitConvCatVolume_Liter, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatVolume_CubicMeter, BigDecimal("1000.0")),
            // US
            UnitDef(R.string.UnitConvCatVolume_TeaspoonUS, BigDecimal("0.00492892159375")),
            UnitDef(R.string.UnitConvCatVolume_TablespoonUS, BigDecimal("0.01478676478125")),
            UnitDef(R.string.UnitConvCatVolume_FluidOunceUS, BigDecimal("0.0295735295625")),
            UnitDef(R.string.UnitConvCatVolume_CupUS, BigDecimal("0.2365882365")),
            UnitDef(R.string.UnitConvCatVolume_PintUS, BigDecimal("0.4731764730")),
            UnitDef(R.string.UnitConvCatVolume_QuartUS, BigDecimal("0.946352946")),
            UnitDef(R.string.UnitConvCatVolume_GallonUS, BigDecimal("3.785411784")),
            // UK
            UnitDef(R.string.UnitConvCatVolume_TeaspoonUK, BigDecimal("0.005919388020833333")),
            UnitDef(R.string.UnitConvCatVolume_TablespoonUK, BigDecimal("0.0177581640625")),
            UnitDef(R.string.UnitConvCatVolume_FluidOunceUK, BigDecimal("0.0284130625")),
            UnitDef(R.string.UnitConvCatVolume_PintUK, BigDecimal("0.56826125")),
            UnitDef(R.string.UnitConvCatVolume_QuartUK, BigDecimal("1.1365225000")),
            UnitDef(R.string.UnitConvCatVolume_GallonUK, BigDecimal("4.54609"))
        ),


        // AREA (metric then imperial) (base = square meter)
        "Area" to listOf(
            UnitDef(R.string.UnitConvCatArea_SquareMillimeter, BigDecimal("1e-6")),
            UnitDef(R.string.UnitConvCatArea_SquareCentimeter, BigDecimal("0.0001")),
            UnitDef(R.string.UnitConvCatArea_SquareMeter, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatArea_Hectare, BigDecimal("10000.0")),
            UnitDef(R.string.UnitConvCatArea_SquareKilometer, BigDecimal("1000000.0")),
            // Imperial
            UnitDef(R.string.UnitConvCatArea_SquareInch, BigDecimal("0.00064516")),
            UnitDef(R.string.UnitConvCatArea_SquareFoot, BigDecimal("0.09290304")),
            UnitDef(R.string.UnitConvCatArea_SquareYard, BigDecimal("0.83612736")),
            UnitDef(R.string.UnitConvCatArea_Acre, BigDecimal("4046.8564224")),
            UnitDef(R.string.UnitConvCatArea_SquareMile, BigDecimal("2.589988110336e6"))
        ),

        // TEMPERATURE (Celsius/Kelvin then Fahrenheit/Rankine) (no base -> calculation in UnitConverterViewModel.convert())
        "Temperature" to listOf(
            UnitDef(R.string.UnitConvCatTemperature_Celsius, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatTemperature_Kelvin, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatTemperature_Fahrenheit, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatTemperature_Rankine, BigDecimal("1.0"))
        ),

        // TIME (ascending) (base = second)
        "Time" to listOf(
            UnitDef(R.string.UnitConvCatTime_Nanosecond, BigDecimal("1e-9")),
            UnitDef(R.string.UnitConvCatTime_Microsecond, BigDecimal("1e-6")),
            UnitDef(R.string.UnitConvCatTime_Millisecond, BigDecimal("0.001")),
            UnitDef(R.string.UnitConvCatTime_Second, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatTime_Minute, BigDecimal("60.0")),
            UnitDef(R.string.UnitConvCatTime_Hour, BigDecimal("3600.0")),
            UnitDef(R.string.UnitConvCatTime_Day, BigDecimal("86400.0")),
            UnitDef(R.string.UnitConvCatTime_Week, BigDecimal("604800.0")),
            UnitDef(R.string.UnitConvCatTime_Month, BigDecimal("2629746.0")),
            UnitDef(R.string.UnitConvCatTime_Year, BigDecimal("31556952.0"))
        ),

        // SPEED (metric then imperial then other) (base = meter per second)
        "Speed" to listOf(
            UnitDef(R.string.UnitConvCatSpeed_MeterPerSecond, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatSpeed_KilometerPerSecond, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatSpeed_MinutePerKilometer, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatSpeed_KilometerPerHour, BigDecimal("0.2777777777777777")),
            UnitDef(R.string.UnitConvCatSpeed_FootPerSecond, BigDecimal("0.3048")),
            UnitDef(R.string.UnitConvCatSpeed_MilePerHour, BigDecimal("0.44704")),
            UnitDef(R.string.UnitConvCatSpeed_Knot, BigDecimal("0.5144444444444444")),
            UnitDef(R.string.UnitConvCatSpeed_Mach, BigDecimal("340.29"))
        ),

        // ENERGY (SI then others) (base = joule)
        "Energy" to listOf(
            UnitDef(R.string.UnitConvCatEnergy_Erg, BigDecimal("1e-7")),
            UnitDef(R.string.UnitConvCatEnergy_Joule, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatEnergy_Kilojoule, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatEnergy_Electronvolt, BigDecimal("1.602176634e-19")),
            UnitDef(R.string.UnitConvCatEnergy_Calorie, BigDecimal("4.184")),
            UnitDef(R.string.UnitConvCatEnergy_Kilocalorie, BigDecimal("4184.0")),
            UnitDef(R.string.UnitConvCatEnergy_WattHour, BigDecimal("3600.0")),
            UnitDef(R.string.UnitConvCatEnergy_KilowattHour, BigDecimal("3600000.0")),
            UnitDef(R.string.UnitConvCatEnergy_FootPound, BigDecimal("1.355817948331400")),
            UnitDef(R.string.UnitConvCatEnergy_BTU, BigDecimal("1055.05585262"))
        ),

        // POWER (base = watt)
        "Power" to listOf(
            UnitDef(R.string.UnitConvCatPower_Watt, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatPower_Kilowatt, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatPower_Megawatt, BigDecimal("1000000.0")),
            UnitDef(R.string.UnitConvCatPower_Horsepower, BigDecimal("745.6998715822702")),
            UnitDef(R.string.UnitConvCatPower_MetricHorsepower, BigDecimal("735.49875"))
        ),

        // PRESSURE (SI then non-SI) (base = pascal)
        "Pressure" to listOf(
            UnitDef(R.string.UnitConvCatPressure_Pascal, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatPressure_Kilopascal, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatPressure_Millibar, BigDecimal("100.0")),
            UnitDef(R.string.UnitConvCatPressure_Bar, BigDecimal("100000.0")),
            UnitDef(R.string.UnitConvCatPressure_Atmosphere, BigDecimal("101325.0")),
            UnitDef(R.string.UnitConvCatPressure_Torr, BigDecimal("133.32236842105263")),
            UnitDef(R.string.UnitConvCatPressure_MmHg, BigDecimal("133.3223874150796")),
            UnitDef(R.string.UnitConvCatPressure_KgfPerCm2, BigDecimal("98066.5")),
            UnitDef(R.string.UnitConvCatPressure_PSI, BigDecimal("6894.757293168361"))
        ),

        // FREQUENCY (base = hertz)
        "Frequency" to listOf(
            UnitDef(R.string.UnitConvCatFrequency_Hertz, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatFrequency_Kilohertz, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatFrequency_Megahertz, BigDecimal("1000000.0")),
            UnitDef(R.string.UnitConvCatFrequency_Gigahertz, BigDecimal("1000000000.0"))
        ),

        // DATA (decimal bits, decimal bytes, binary) (base = bit)
        "Data" to listOf(
            // Bits (SI)
            UnitDef(R.string.UnitConvCatData_Bit, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatData_Kilobit, BigDecimal("1000.0")),
            UnitDef(R.string.UnitConvCatData_Megabit, BigDecimal("1000000.0")),
            UnitDef(R.string.UnitConvCatData_Gigabit, BigDecimal("1000000000.0")),
            UnitDef(R.string.UnitConvCatData_Terabit, BigDecimal("1000000000000.0")),
            UnitDef(R.string.UnitConvCatData_Petabit, BigDecimal("1000000000000000.0")),
            UnitDef(R.string.UnitConvCatData_Exabit, BigDecimal("1000000000000000000.0")),

            // Bytes (SI)
            UnitDef(R.string.UnitConvCatData_Byte, BigDecimal("8.0")),
            UnitDef(R.string.UnitConvCatData_Kilobyte, BigDecimal("8000.0")),
            UnitDef(R.string.UnitConvCatData_Megabyte, BigDecimal("8000000.0")),
            UnitDef(R.string.UnitConvCatData_Gigabyte, BigDecimal("8000000000.0")),
            UnitDef(R.string.UnitConvCatData_Terabyte, BigDecimal("8000000000000.0")),
            UnitDef(R.string.UnitConvCatData_Petabyte, BigDecimal("8000000000000000.0")),
            UnitDef(R.string.UnitConvCatData_Exabyte, BigDecimal("8000000000000000000.0")),

            // Binary (IEC)
            UnitDef(R.string.UnitConvCatData_Kibibyte, BigDecimal("8192.0")),
            UnitDef(R.string.UnitConvCatData_Mebibyte, BigDecimal("8388608.0")),
            UnitDef(R.string.UnitConvCatData_Gibibyte, BigDecimal("8589934592.0")),
            UnitDef(R.string.UnitConvCatData_Tebibyte, BigDecimal("8796093022208.0")),
            UnitDef(R.string.UnitConvCatData_Pebibyte, BigDecimal("9007199254740992.0")),
            UnitDef(R.string.UnitConvCatData_Exbibyte, BigDecimal("9223372036854775808.0"))
        ),

        // FUEL ECONOMY (special-case conversion L/100km ↔ mpg) (no base -> calculation in UnitConverterViewModel.convert())
        "FuelEconomy" to listOf(
            UnitDef(R.string.UnitConvCatFuelEconomy_LitersPer100km, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatFuelEconomy_MPG_US, BigDecimal("235.214583")),
            UnitDef(R.string.UnitConvCatFuelEconomy_MPG_UK, BigDecimal("282.481053"))
        ),

        // PLANE ANGLE (base = radian)
        "PlaneAngle" to listOf(
            UnitDef(R.string.UnitConvCatPlaneAngle_Radian, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatPlaneAngle_Degree, BigDecimal("0.017453292519943295")),
            UnitDef(R.string.UnitConvCatPlaneAngle_Grad, BigDecimal("0.01570796326794896")),
            UnitDef(R.string.UnitConvCatPlaneAngle_Arcminute, BigDecimal("0.0002908882086657216")),
            UnitDef(R.string.UnitConvCatPlaneAngle_Arcsecond, BigDecimal("4.84813681109536e-6"))
        ),

        // AMOUNT OF SUBSTANCE (metric prefixes) (base = mole)
        "Amount" to listOf(
            UnitDef(R.string.UnitConvCatAmount_Picomole, BigDecimal("1e-12")),
            UnitDef(R.string.UnitConvCatAmount_Nanomole, BigDecimal("1e-9")),
            UnitDef(R.string.UnitConvCatAmount_Micromole, BigDecimal("1e-6")),
            UnitDef(R.string.UnitConvCatAmount_Millimole, BigDecimal("1e-3")),
            UnitDef(R.string.UnitConvCatAmount_Mole, BigDecimal("1.0")),
            UnitDef(R.string.UnitConvCatAmount_Kilomole, BigDecimal("1e3"))
        )
    )
}