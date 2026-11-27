package com.sravanthi.quickcart.feature.store

import android.content.Context
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.test.core.app.ApplicationProvider
import com.sravanthi.quickcart.core.model.DeepLinks
import com.sravanthi.quickcart.feature.store.navigation.STORE_ID_ARG
import com.sravanthi.quickcart.feature.store.navigation.STORE_ROUTE
import com.sravanthi.quickcart.feature.store.navigation.storeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoreDeepLinkTest {

    private val graph = NavHostController(ApplicationProvider.getApplicationContext<Context>()).run {
        navigatorProvider.addNavigator(ComposeNavigator())
        createGraph(startDestination = "$STORE_ROUTE/{$STORE_ID_ARG}") {
            storeScreen(onBack = {}, onViewCart = {})
        }
    }

    private fun match(uri: String) = graph.matchDeepLink(NavDeepLinkRequest.Builder.fromUri(uri.toUri()).build())

    @Test
    fun `a store link opens that store`() {
        val match = checkNotNull(match(DeepLinks.store(8)))
        assertEquals(8L, match.matchingArgs?.getLong(STORE_ID_ARG))
    }

    @Test
    fun `a store link without a numeric id is ignored`() {
        assertNull(match("quickcart://store/freshmart"))
    }
}
