package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.model.NetworkMenuItem
import com.sravanthi.quickcart.core.network.model.NetworkStore
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the bundled demo data, so a typo fails the build instead of crashing the app. */
class MarketplaceDataTest {

    @Serializable
    private data class Data(val stores: List<NetworkStore>, val items: List<NetworkMenuItem>)

    private val data = NetworkModule.provideJson()
        .decodeFromString<Data>(File("src/main/assets/marketplace.json").readText())

    @Test
    fun `ids are unique`() {
        assertEquals(data.stores.size, data.stores.map { it.id }.toSet().size)
        assertEquals(data.items.size, data.items.map { it.id }.toSet().size)
    }

    @Test
    fun `every item belongs to an existing store and every store has a menu`() {
        val storeIds = data.stores.map { it.id }.toSet()
        assertTrue(data.items.all { it.storeId in storeIds })
        data.stores.forEach { store -> assertTrue("${store.name} has items", data.items.count { it.storeId == store.id } >= 3) }
    }

    @Test
    fun `stores and items map to valid app models`() {
        data.stores.forEach { store ->
            val model = store.asExternalModel()
            assertTrue(model.deliveryMinMinutes in 5..model.deliveryMaxMinutes)
            assertTrue(model.rating in 0.0..5.0)
            assertTrue(model.imageUrl.startsWith("https://"))
        }
        data.items.forEach { item ->
            val model = item.asExternalModel()
            assertTrue("${item.name} has a price", model.priceCents > 0)
            assertTrue(model.imageUrl.startsWith("https://"))
            assertTrue(model.section.isNotBlank())
        }
    }

    @Test
    fun `the marketplace covers restaurants and groceries`() {
        val types = data.stores.map { it.type }.toSet()
        assertTrue(types.containsAll(setOf("RESTAURANT", "GROCERY", "CONVENIENCE", "DESSERT", "DRINKS")))
    }
}
