package com.sravanthi.quickcart.feature.cart.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.quickcart.feature.cart.CartRoute

private const val CART_ROUTE = "cart"

fun NavController.navigateToCart() = navigate(CART_ROUTE) { launchSingleTop = true }

fun NavGraphBuilder.cartScreen(onBack: () -> Unit, onBrowseStores: () -> Unit) {
    composable(route = CART_ROUTE) { CartRoute(onBack = onBack, onBrowseStores = onBrowseStores) }
}
