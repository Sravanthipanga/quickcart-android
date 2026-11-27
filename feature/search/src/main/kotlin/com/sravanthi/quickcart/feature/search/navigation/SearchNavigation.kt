package com.sravanthi.quickcart.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.quickcart.feature.search.SearchRoute

private const val SEARCH_ROUTE = "search"

fun NavController.navigateToSearch() = navigate(SEARCH_ROUTE)

fun NavGraphBuilder.searchScreen(onBack: () -> Unit, onStoreClick: (Long) -> Unit) {
    composable(route = SEARCH_ROUTE) { SearchRoute(onBack = onBack, onStoreClick = onStoreClick) }
}
