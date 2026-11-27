package com.sravanthi.quickcart.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.quickcart.feature.home.HomeRoute

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen(onStoreClick: (Long) -> Unit, onSearchClick: () -> Unit, onCartClick: () -> Unit) {
    composable(route = HOME_ROUTE) {
        HomeRoute(onStoreClick = onStoreClick, onSearchClick = onSearchClick, onCartClick = onCartClick)
    }
}
