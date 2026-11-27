package com.sravanthi.quickcart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sravanthi.quickcart.feature.catalog.navigation.CATALOG_ROUTE
import com.sravanthi.quickcart.feature.catalog.navigation.catalogScreen

@Composable
fun QuickCartNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = CATALOG_ROUTE, modifier = modifier) {
        // Product details arrive in the next milestone.
        catalogScreen(onProductClick = { })
    }
}
