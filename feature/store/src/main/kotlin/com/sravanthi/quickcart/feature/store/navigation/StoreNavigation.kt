package com.sravanthi.quickcart.feature.store.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sravanthi.quickcart.core.model.DeepLinks
import com.sravanthi.quickcart.feature.store.StoreRoute

internal const val STORE_ID_ARG = "storeId"
internal const val STORE_ROUTE = "store"

fun NavController.navigateToStore(storeId: Long) = navigate("$STORE_ROUTE/$storeId")

fun NavGraphBuilder.storeScreen(onBack: () -> Unit, onViewCart: () -> Unit) {
    composable(
        route = "$STORE_ROUTE/{$STORE_ID_ARG}",
        arguments = listOf(navArgument(STORE_ID_ARG) { type = NavType.LongType }),
        deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.STORE_PATTERN }),
    ) {
        StoreRoute(onBack = onBack, onViewCart = onViewCart)
    }
}
