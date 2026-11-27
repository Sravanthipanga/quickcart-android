package com.sravanthi.quickcart.feature.orders

import app.cash.turbine.test
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeOrderRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.core.testing.sampleOrders
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = FakeCartRepository()
    private val orders = FakeOrderRepository(cart)
    private val viewModel by lazy { OrdersViewModel(orders) }

    @Test
    fun `no orders shows the empty state`() = runTest {
        viewModel.uiState.test { assertEquals(OrdersUiState.Empty, expectMostRecentItem()) }
    }

    @Test
    fun `orders split into in progress and past`() = runTest {
        orders.orders.value = sampleOrders()
        viewModel.uiState.test {
            val state = expectMostRecentItem() as OrdersUiState.Ready
            assertEquals(listOf("QC-1046"), state.active.map { it.id })
            assertEquals(listOf("QC-1044", "QC-1042"), state.past.map { it.id })
        }
    }

    @Test
    fun `reorder refills the cart and then opens it`() = runTest {
        orders.orders.value = sampleOrders()
        var opened = false

        viewModel.reorder("QC-1044") { opened = true }

        assertEquals(true, opened)
        assertEquals("FreshMart Grocery", cart.cart.value.store?.name)
        assertEquals(7, cart.cart.value.itemCount)
        assertEquals(OrderStatus.DELIVERED, orders.orders.value.first { it.id == "QC-1044" }.status)
    }
}
