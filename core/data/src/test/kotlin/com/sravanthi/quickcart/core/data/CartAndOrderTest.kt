package com.sravanthi.quickcart.core.data

import com.sravanthi.quickcart.core.data.repository.NetworkOrderRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstCartRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstStoreRepository
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.model.AddToCartResult
import com.sravanthi.quickcart.core.model.MenuItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.LocalTime

/** The cart and checkout against the real mock server and bundled data, with an in-memory database. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CartAndOrderTest {

    private val marketplace = TestMarketplace()
    private val stores = OfflineFirstStoreRepository(marketplace.api, marketplace.database, Clock.systemUTC())
    private val cart = OfflineFirstCartRepository(marketplace.database, Clock.systemUTC())
    private val orders = NetworkOrderRepository(marketplace.api, marketplace.database, marketplace.json, Clock.systemUTC())

    private lateinit var pizza: MenuItem // $14.99 at Bella Napoli (minimum $12)
    private lateinit var bruschetta: MenuItem // $8.99 at Bella Napoli
    private lateinit var milk: MenuItem // FreshMart

    @Before
    fun loadMenus() = runTest {
        stores.refreshStore(1)
        stores.refreshStore(8)
        val pizzeria = stores.observeMenu(1).first()
        pizza = pizzeria.first { it.name == "Classic Margherita Pizza" }
        bruschetta = pizzeria.first { it.name == "Tomato Basil Bruschetta" }
        milk = stores.observeMenu(8).first().first { it.name == "Milk" }
    }

    @After
    fun tearDown() = marketplace.database.close()

    @Test
    fun `adding twice increases the quantity and the cart knows its store`() = runTest {
        cart.add(pizza)
        cart.add(pizza)
        cart.add(bruschetta)

        val current = cart.observeCart().first()
        assertEquals("Bella Napoli Pizzeria", current.store?.name)
        assertEquals(2, current.quantityOf(pizza.id))
        assertEquals(3, current.itemCount)
        assertEquals(1499L * 2 + 899, current.subtotalCents)
    }

    @Test
    fun `adding from another store asks first, and starting a new cart switches stores`() = runTest {
        cart.add(pizza)

        val result = cart.add(milk)
        assertEquals(AddToCartResult.DifferentStore(1, "Bella Napoli Pizzeria"), result)
        assertEquals(listOf(pizza.id), cart.observeCart().first().lines.map { it.item.id })

        cart.startNewCartWith(milk)
        val switched = cart.observeCart().first()
        assertEquals("FreshMart Grocery", switched.store?.name)
        assertEquals(listOf(milk.id), switched.lines.map { it.item.id })
    }

    @Test
    fun `setting a quantity to zero removes the line`() = runTest {
        cart.add(pizza)
        cart.add(bruschetta)
        cart.setQuantity(pizza.id, 0)
        assertEquals(listOf(bruschetta.id), cart.observeCart().first().lines.map { it.item.id })
    }

    @Test
    fun `placing an order uses server prices and empties the cart`() = runTest {
        cart.add(pizza)
        cart.add(pizza)

        val result = orders.placeOrder() as PlaceOrderResult.Placed

        assertEquals("Bella Napoli Pizzeria", result.confirmation.storeName)
        assertEquals(2998L, result.confirmation.pricing.subtotalCents)
        assertTrue(cart.observeCart().first().isEmpty)
    }

    @Test
    fun `a rejected order keeps the cart and shows the server's reason`() = runTest {
        cart.add(bruschetta) // $8.99 is below the $12 minimum

        val result = orders.placeOrder() as PlaceOrderResult.Rejected

        assertTrue(result.message.contains("minimum"))
        assertEquals(1, cart.observeCart().first().itemCount)
    }

    @Test
    fun `a closed store rejects the order with its name`() = runTest {
        cart.add(pizza)
        marketplace.serverTime = LocalTime.of(8, 0)

        assertEquals(PlaceOrderResult.Rejected("Bella Napoli Pizzeria is closed right now"), orders.placeOrder())
    }

    @Test
    fun `offline checkout keeps the cart for a retry`() = runTest {
        cart.add(pizza)
        marketplace.offline = true

        assertEquals(PlaceOrderResult.Offline, orders.placeOrder())
        assertEquals(1, cart.observeCart().first().itemCount)
    }
}
