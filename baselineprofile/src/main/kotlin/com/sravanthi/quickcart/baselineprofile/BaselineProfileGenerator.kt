package com.sravanthi.quickcart.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records which code runs during startup and the main journeys. The result ships with the app
 * so Android compiles that code ahead of time at install, instead of interpreting it at first use.
 *
 * Generate with: ./gradlew :app:generateReleaseBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        waitForStores()
        scrollStores()
        openFirstStoreAndScrollMenu()
        searchFor("pizza")
    }
}
