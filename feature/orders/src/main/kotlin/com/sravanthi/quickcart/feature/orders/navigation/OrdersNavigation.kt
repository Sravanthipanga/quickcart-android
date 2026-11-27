package com.sravanthi.quickcart.feature.orders.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sravanthi.quickcart.feature.orders.OrdersRoute
import com.sravanthi.quickcart.feature.orders.TrackOrderRoute

internal const val ORDER_ID_ARG = "orderId"
private const val ORDERS_ROUTE = "orders"
private const val TRACK_ROUTE = "orders/track"

fun NavController.navigateToOrders() = navigate(ORDERS_ROUTE) { launchSingleTop = true }

fun NavController.navigateToOrderTracking(orderId: String) = navigate("$TRACK_ROUTE/$orderId")

fun NavGraphBuilder.ordersScreens(
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onOpenCart: () -> Unit,
    onBrowseStores: () -> Unit,
) {
    composable(route = ORDERS_ROUTE) {
        OrdersRoute(onBack = onBack, onOrderClick = onOrderClick, onOpenCart = onOpenCart, onBrowseStores = onBrowseStores)
    }
    composable(
        route = "$TRACK_ROUTE/{$ORDER_ID_ARG}",
        arguments = listOf(navArgument(ORDER_ID_ARG) { type = NavType.StringType }),
    ) {
        TrackOrderRoute(onBack = onBack, onOpenCart = onOpenCart)
    }
}
