package com.sravanthi.quickcart.core.data

import com.sravanthi.quickcart.core.data.repository.OfflineFirstSearchRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstStoreRepository
import com.sravanthi.quickcart.core.model.SearchResults
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchRepositoryTest {

    private val marketplace = TestMarketplace()
    private val search = OfflineFirstSearchRepository(marketplace.api, marketplace.database)
    private val stores = OfflineFirstStoreRepository(marketplace.api, marketplace.database, Clock.systemUTC())

    @After
    fun tearDown() = marketplace.database.close()

    @Test
    fun `online search covers every store and names each item's store`() = runTest {
        val outcome = search.search("tiramisu")

        assertFalse(outcome.fromCache)
        assertEquals(
            setOf("Bella Napoli Pizzeria", "Sweet Spot Desserts"),
            outcome.results.items.map { it.storeName }.toSet(),
        )
    }

    @Test
    fun `offline search falls back to cached stores and menus`() = runTest {
        stores.refreshStore(1)
        stores.refreshStore(10)
        marketplace.offline = true

        val outcome = search.search("tiramisu")

        assertTrue(outcome.fromCache)
        assertEquals(
            setOf("Bella Napoli Pizzeria", "Sweet Spot Desserts"),
            outcome.results.items.map { it.storeName }.toSet(),
        )
    }

    @Test
    fun `offline search only finds what was cached`() = runTest {
        stores.refreshStore(1)
        marketplace.offline = true
        assertEquals(listOf("Bella Napoli Pizzeria"), search.search("tiramisu").results.items.map { it.storeName })
    }

    @Test
    fun `queries under two characters return nothing without calling the network`() = runTest {
        assertEquals(SearchResults.Empty, search.search(" p ").results)
        assertEquals(0, marketplace.requests)
    }
}
