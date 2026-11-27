package com.sravanthi.quickcart.feature.orders.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sravanthi.quickcart.core.model.DeepLinks
import com.sravanthi.quickcart.feature.orders.OrdersRoute
import com.sravanthi.quickcart.feature.orders.TrackOrderRoute

internal const val ORDER_ID_ARG = "orderId"
internal const val ORDERS_ROUTE = "orders"
internal const val TRACK_ROUTE = "orders/track"

fun NavController.navigateToOrders() = navigate(ORDERS_ROUTE) { launchSingleTop = true }

fun NavController.navigateToOrderTracking(orderId: String) = navigate("$TRACK_ROUTE/$orderId")

fun NavGraphBuilder.ordersScreens(
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onOpenCart: () -> Unit,
    onBrowseStores: () -> Unit,
) {
    composable(route = ORDERS_ROUTE, deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.ORDERS })) {
        OrdersRoute(onBack = onBack, onOrderClick = onOrderClick, onOpenCart = onOpenCart, onBrowseStores = onBrowseStores)
    }
    composable(
        route = "$TRACK_ROUTE/{$ORDER_ID_ARG}",
        arguments = listOf(navArgument(ORDER_ID_ARG) { type = NavType.StringType }),
        // Opened from order notifications.
        deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.ORDER_PATTERN }),
    ) {
        TrackOrderRoute(onBack = onBack, onOpenCart = onOpenCart)
    }
}
