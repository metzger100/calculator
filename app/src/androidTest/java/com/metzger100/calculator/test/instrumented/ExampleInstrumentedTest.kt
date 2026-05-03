package com.metzger100.calculator.test.instrumented

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.metzger100.calculator.ui.navigation.NavItem
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        MatcherAssert.assertThat(
            appContext.packageName,
            `is`("com.metzger100.calculator")
        )
        MatcherAssert.assertThat(
            appContext.getString(NavItem.Currency.labelRes).isNotBlank(),
            `is`(true)
        )
    }
}
