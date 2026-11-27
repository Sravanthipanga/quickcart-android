package com.sravanthi.quickcart.feature.store

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.model.bySection
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
import com.sravanthi.quickcart.core.testing.sampleGroceries
import com.sravanthi.quickcart.core.testing.sampleMenu
import com.sravanthi.quickcart.core.testing.sampleStores
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class StoreScreenshotTest {

    @Test
    fun restaurant() = captureRoboImage("$SCREENSHOT_DIR/store_restaurant.png") {
        QuickCartTheme {
            StoreScreen(
                StoreUiState.Ready(sampleStores.first { it.id == 1L }, sampleMenu.bySection(), isOpen = true, isRefreshing = false, isOffline = false),
                onBack = {},
                onRetry = {},
            )
        }
    }

    @Test
    fun groceryOffline() = captureRoboImage("$SCREENSHOT_DIR/store_grocery_offline.png") {
        QuickCartTheme {
            StoreScreen(
                StoreUiState.Ready(sampleStores.first { it.id == 8L }, sampleGroceries.bySection(), isOpen = true, isRefreshing = false, isOffline = true),
                onBack = {},
                onRetry = {},
            )
        }
    }
}
