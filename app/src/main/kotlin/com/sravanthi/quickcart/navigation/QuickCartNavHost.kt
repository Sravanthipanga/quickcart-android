package com.sravanthi.quickcart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sravanthi.quickcart.feature.cart.navigation.cartScreen
import com.sravanthi.quickcart.feature.cart.navigation.navigateToCart
import com.sravanthi.quickcart.feature.home.navigation.HOME_ROUTE
import com.sravanthi.quickcart.feature.home.navigation.homeScreen
import com.sravanthi.quickcart.feature.search.navigation.navigateToSearch
import com.sravanthi.quickcart.feature.search.navigation.searchScreen
import com.sravanthi.quickcart.feature.store.navigation.navigateToStore
import com.sravanthi.quickcart.feature.store.navigation.storeScreen

@Composable
fun QuickCartNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = HOME_ROUTE, modifier = modifier) {
        homeScreen(
            onStoreClick = navController::navigateToStore,
            onSearchClick = navController::navigateToSearch,
            onCartClick = navController::navigateToCart,
        )
        searchScreen(
            onBack = navController::popBackStack,
            onStoreClick = navController::navigateToStore,
        )
        storeScreen(
            onBack = navController::popBackStack,
            onViewCart = navController::navigateToCart,
        )
        cartScreen(
            onBack = navController::popBackStack,
            // After ordering (or from an empty cart), return to the store list.
            onBrowseStores = { navController.popBackStack(HOME_ROUTE, inclusive = false) },
        )
    }
}
