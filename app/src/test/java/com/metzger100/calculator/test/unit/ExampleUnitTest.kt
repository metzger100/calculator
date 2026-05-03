package com.metzger100.calculator.test.unit

import com.metzger100.calculator.features.calculator.model.CalculatorMode
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun calculatorModeHasStandardAndScientificVariants() {
        MatcherAssert.assertThat(
            CalculatorMode.valueOf("STANDARD"),
            CoreMatchers.`is`(CalculatorMode.STANDARD)
        )
        MatcherAssert.assertThat(
            CalculatorMode.valueOf("SCIENTIFIC"),
            CoreMatchers.`is`(CalculatorMode.SCIENTIFIC)
        )
    }
}
