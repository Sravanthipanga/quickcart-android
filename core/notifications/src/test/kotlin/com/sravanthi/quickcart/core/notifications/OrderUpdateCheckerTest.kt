package com.sravanthi.quickcart.core.notifications

import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeOrderRepository
import com.sravanthi.quickcart.core.testing.sampleMenu
import com.sravanthi.quickcart.core.testing.sampleOrders
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderUpdateCheckerTest {

    private val cart = FakeCartRepository()
    private val orders = FakeOrderRepository(cart)
    private val notifier = RecordingNotifier()
    private val checker = OrderUpdateChecker(orders, notifier)

    private suspend fun placeOrder(): String {
        cart.add(sampleMenu[0])
        orders.placeOrder()
        return orders.orders.value.first().id
    }

    @Test
    fun `notifies once when the status moves, not on every check`() = runTest {
        val id = placeOrder()
        orders.serverStatus[id] = OrderStatus.PREPARING

        assertTrue("still active, check again", checker.checkActiveOrders())
        assertEquals(listOf(OrderStatus.PREPARING), notifier.notified.map { it.status })

        checker.checkActiveOrders()
        assertEquals("no change, no new notification", 1, notifier.notified.size)
    }

    @Test
    fun `delivery is announced and checks stop`() = runTest {
        val id = placeOrder()
        orders.serverStatus[id] = OrderStatus.DELIVERED

        assertFalse(checker.checkActiveOrders())
        assertEquals(OrderStatus.DELIVERED, notifier.notified.single().status)
    }

    @Test
    fun `offline checks stay quiet and keep trying`() = runTest {
        val id = placeOrder()
        orders.serverStatus[id] = OrderStatus.ON_THE_WAY
        orders.online = false

        assertTrue(checker.checkActiveOrders())
        assertTrue(notifier.notified.isEmpty())
    }

    @Test
    fun `past orders are never checked`() = runTest {
        orders.orders.value = sampleOrders().filterNot { it.isActive }

        assertFalse(checker.checkActiveOrders())
        assertEquals(0, orders.refreshCount)
    }
}
