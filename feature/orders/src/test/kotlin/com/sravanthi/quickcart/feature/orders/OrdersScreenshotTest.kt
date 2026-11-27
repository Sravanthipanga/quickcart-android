package com.sravanthi.quickcart.feature.orders

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
import com.sravanthi.quickcart.core.testing.sampleOrders
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class OrdersScreenshotTest {

    private val orders = sampleOrders()

    @Test
    fun ordersList() = captureRoboImage("$SCREENSHOT_DIR/orders.png") {
        QuickCartTheme {
            OrdersScreen(
                OrdersUiState.Ready(active = orders.filter { it.isActive }, past = orders.filterNot { it.isActive }),
                onBack = {}, onOrderClick = {}, onReorder = {}, onBrowseStores = {},
            )
        }
    }

    @Test
    fun tracking() = captureRoboImage("$SCREENSHOT_DIR/order_tracking.png") {
        QuickCartTheme {
            TrackOrderScreen(TrackOrderUiState.Ready(orders.first(), isOffline = false), onBack = {}, onReorder = {})
        }
    }
}
