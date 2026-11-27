package com.sravanthi.quickcart.feature.orders

import android.content.Context
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.test.core.app.ApplicationProvider
import com.sravanthi.quickcart.core.model.DeepLinks
import com.sravanthi.quickcart.feature.orders.navigation.ORDERS_ROUTE
import com.sravanthi.quickcart.feature.orders.navigation.ORDER_ID_ARG
import com.sravanthi.quickcart.feature.orders.navigation.TRACK_ROUTE
import com.sravanthi.quickcart.feature.orders.navigation.ordersScreens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OrdersDeepLinkTest {

    private val graph = NavHostController(ApplicationProvider.getApplicationContext<Context>()).run {
        navigatorProvider.addNavigator(ComposeNavigator())
        createGraph(startDestination = ORDERS_ROUTE) {
            ordersScreens(onBack = {}, onOrderClick = {}, onOpenCart = {}, onBrowseStores = {})
        }
    }

    private fun match(uri: String) = graph.matchDeepLink(NavDeepLinkRequest.Builder.fromUri(uri.toUri()).build())

    @Test
    fun `an order link opens that order's tracking`() {
        val match = checkNotNull(match(DeepLinks.order("QC-1046")))
        assertEquals("$TRACK_ROUTE/{$ORDER_ID_ARG}", match.destination.route)
        assertEquals("QC-1046", match.matchingArgs?.getString(ORDER_ID_ARG))
    }

    @Test
    fun `the orders link opens the history`() {
        assertEquals(ORDERS_ROUTE, match(DeepLinks.ORDERS)?.destination?.route)
    }

    @Test
    fun `links for other apps don't match`() {
        assertNull(match("https://example.com/orders/QC-1046"))
    }
}
