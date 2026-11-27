package com.sravanthi.quickcart.core.data

import com.sravanthi.quickcart.core.data.repository.NetworkOrderRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstCartRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstStoreRepository
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.model.OrderStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock

/** Order history and live status against the real mock server, with an in-memory database. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OrderHistoryTest {

    private val marketplace = TestMarketplace()
    private val stores = OfflineFirstStoreRepository(marketplace.api, marketplace.database, Clock.systemUTC())
    private val cart = OfflineFirstCartRepository(marketplace.database, Clock.systemUTC())
    private val orders = NetworkOrderRepository(marketplace.api, marketplace.database, marketplace.json, Clock.systemUTC())

    @Before
    fun loadMenus() = runTest {
        stores.refreshStore(1)
        stores.refreshStore(8)
    }

    @After
    fun tearDown() = marketplace.database.close()

    private suspend fun orderPizzas(quantity: Int = 2): String {
        val pizza = stores.observeMenu(1).first().first { it.name == "Classic Margherita Pizza" }
        repeat(quantity) { cart.add(pizza) }
        return (orders.placeOrder() as PlaceOrderResult.Placed).confirmation.orderId
    }

    @Test
    fun `a placed order is saved with its lines, prices and store`() = runTest {
        val id = orderPizzas()

        val order = orders.observeOrder(id).first()!!
        assertEquals("Bella Napoli Pizzeria", order.storeName)
        assertEquals(OrderStatus.PLACED, order.status)
        assertEquals(listOf("Classic Margherita Pizza" to 2), order.lines.map { it.name to it.quantity })
        assertEquals(2998L, order.pricing.subtotalCents)
        assertTrue(order.storeImageUrl.startsWith("https://"))
    }

    @Test
    fun `history is newest first`() = runTest {
        val first = orderPizzas(quantity = 1)
        marketplace.serverTime = marketplace.serverTime.plusMinutes(1)
        val second = orderPizzas(quantity = 1)

        assertEquals(listOf(second, first), orders.observeOrders().first().map { it.id })
    }

    @Test
    fun `refreshing follows the server's progress`() = runTest {
        val id = orderPizzas()

        marketplace.serverTime = marketplace.serverTime.plusMinutes(10)
        assertTrue(orders.refreshStatus(id))
        assertEquals(OrderStatus.PREPARING, orders.observeOrder(id).first()?.status)

        marketplace.serverTime = marketplace.serverTime.plusMinutes(30)
        orders.refreshStatus(id)
        assertEquals(OrderStatus.DELIVERED, orders.observeOrder(id).first()?.status)
    }

    @Test
    fun `offline refresh keeps the last known status`() = runTest {
        val id = orderPizzas()
        marketplace.serverTime = marketplace.serverTime.plusMinutes(10)
        orders.refreshStatus(id)
        marketplace.offline = true

        assertFalse(orders.refreshStatus(id))
        assertEquals(OrderStatus.PREPARING, orders.observeOrder(id).first()?.status)
    }

    @Test
    fun `reorder refills the cart from a past order, replacing what was there`() = runTest {
        val id = orderPizzas(quantity = 3)
        cart.add(stores.observeMenu(8).first().first { it.name == "Milk" })

        orders.reorder(id)

        val refilled = cart.observeCart().first()
        assertEquals("Bella Napoli Pizzeria", refilled.store?.name)
        assertEquals(3, refilled.itemCount)
        assertEquals(1499L * 3, refilled.subtotalCents)
    }

    @Test
    fun `a rejected order is not saved to history`() = runTest {
        cart.add(stores.observeMenu(1).first().first { it.name == "Tomato Basil Bruschetta" }) // below minimum
        orders.placeOrder()
        assertTrue(orders.observeOrders().first().isEmpty())
    }
}
