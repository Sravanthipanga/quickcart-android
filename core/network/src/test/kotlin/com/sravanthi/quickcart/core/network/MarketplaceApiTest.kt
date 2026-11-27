package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.model.ItemTag
import com.sravanthi.quickcart.core.model.StoreType
import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.mock.MockMarketplaceInterceptor
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.File
import java.time.LocalTime

/** Exercises the real Retrofit client against the in-app mock server and the bundled marketplace data. */
class MarketplaceApiTest {

    private val json = NetworkModule.provideJson()
    private val api = NetworkModule.createApi(
        baseUrl = "https://api.quickcart.example/v1/",
        client = OkHttpClient.Builder()
            .addInterceptor(MockMarketplaceInterceptor({ File(MARKETPLACE).readText() }, json, latencyMillis = 0))
            .build(),
        json = json,
    )

    @Test
    fun `stores are served in pages`() = runTest {
        val first = api.getStores(limit = 4, skip = 0)
        val last = api.getStores(limit = 4, skip = 8)

        assertEquals(11, first.total)
        assertEquals(listOf(1L, 2L, 3L, 4L), first.stores.map { it.id })
        assertEquals(listOf(9L, 10L, 11L), last.stores.map { it.id })
    }

    @Test
    fun `a store and its menu map to app models`() = runTest {
        val store = api.getStore(4).asExternalModel()
        val menu = api.getMenu(4).items.map { it.asExternalModel() }

        assertEquals("Taco Fiesta", store.name)
        assertEquals(StoreType.RESTAURANT, store.type)
        assertEquals(LocalTime.of(2, 0), store.closesAt)
        assertTrue(menu.isNotEmpty() && menu.all { it.storeId == 4L })
        assertTrue(menu.first { it.name == "Mexican Street Corn (Elote)" }.tags.contains(ItemTag.VEGETARIAN))
    }

    @Test
    fun `search matches stores by cuisine and items across stores`() = runTest {
        // Tiramisu is on the menu at both the pizzeria and the dessert shop.
        val results = api.search("TIRAMISU")
        assertTrue(results.items.all { it.item.name.contains("tiramisu", ignoreCase = true) })
        assertEquals(setOf("Bella Napoli Pizzeria", "Sweet Spot Desserts"), results.items.map { it.storeName }.toSet())

        assertEquals(listOf("Bella Napoli Pizzeria"), api.search("italian").stores.map { it.name })
    }

    @Test
    fun `very short searches return nothing`() = runTest {
        val results = api.search("a")
        assertTrue(results.stores.isEmpty() && results.items.isEmpty())
    }

    @Test(expected = HttpException::class)
    fun `unknown stores are a 404`() = runTest {
        api.getStore(999)
    }

    private companion object {
        // Unit tests run with the module as the working directory.
        const val MARKETPLACE = "src/main/assets/marketplace.json"
    }
}
