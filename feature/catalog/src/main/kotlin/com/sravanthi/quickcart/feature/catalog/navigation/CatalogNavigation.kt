package com.sravanthi.quickcart.feature.catalog.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.quickcart.feature.catalog.CatalogRoute

const val CATALOG_ROUTE = "catalog"

fun NavGraphBuilder.catalogScreen(onProductClick: (Long) -> Unit) {
    composable(route = CATALOG_ROUTE) { CatalogRoute(onProductClick = onProductClick) }
}
