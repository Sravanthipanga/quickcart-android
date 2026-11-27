package com.sravanthi.quickcart.feature.cart

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.CartLine
import com.sravanthi.quickcart.core.model.OrderConfirmation
import com.sravanthi.quickcart.core.model.priceOrder
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
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
class CartScreenshotTest {

    private val store = sampleStores.first { it.id == 1L }
    private val cart = Cart(store, listOf(CartLine(sampleMenu[0], 2), CartLine(sampleMenu[4], 1), CartLine(sampleMenu[5], 1)))
    private val pricing = priceOrder(cart.subtotalCents, store)

    @Test
    fun cart() = captureRoboImage("$SCREENSHOT_DIR/cart.png") {
        QuickCartTheme {
            CartScreen(
                CartUiState.Ready(cart, pricing, isStoreOpen = true, isPlacing = false, error = null, canPlaceOrder = true),
                onBack = {}, onBrowseStores = {}, onSetQuantity = { _, _ -> }, onPlaceOrder = {},
            )
        }
    }

    @Test
    fun orderPlaced() = captureRoboImage("$SCREENSHOT_DIR/order_placed.png") {
        QuickCartTheme {
            CartScreen(
                CartUiState.Placed(OrderConfirmation("QC-1042", store.name, pricing, 25, 35, java.time.Instant.parse("2026-03-10T19:00:00Z"))),
                onBack = {}, onBrowseStores = {}, onSetQuantity = { _, _ -> }, onPlaceOrder = {},
            )
        }
    }
}
