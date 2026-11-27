package com.sravanthi.quickcart.feature.orders

import androidx.lifecycle.SavedStateHandle
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeOrderRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.core.testing.sampleMenu
import com.sravanthi.quickcart.feature.orders.navigation.ORDER_ID_ARG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackOrderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val cart = FakeCartRepository()
    private val orders = FakeOrderRepository(cart)
    private val poll = TrackOrderViewModel.POLL_INTERVAL_MILLIS

    private fun TestScope.trackNewOrder(): Pair<TrackOrderViewModel, String> {
        var id = ""
        backgroundScope.launch {
            cart.add(sampleMenu[0])
            id = (orders.placeOrder() as com.sravanthi.quickcart.core.data.repository.PlaceOrderResult.Placed).confirmation.orderId
        }
        runCurrent()
        val viewModel = TrackOrderViewModel(SavedStateHandle(mapOf(ORDER_ID_ARG to id)), orders)
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        return viewModel to id
    }

    private fun TrackOrderViewModel.ready() = uiState.value as TrackOrderUiState.Ready

    @Test
    fun `polls every 15 seconds and shows the server's status`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, id) = trackNewOrder()
        assertEquals(1, orders.refreshCount)
        assertEquals(OrderStatus.PLACED, viewModel.ready().order.status)

        orders.serverStatus[id] = OrderStatus.PREPARING
        advanceTimeBy(poll)
        runCurrent()
        assertEquals(2, orders.refreshCount)
        assertEquals(OrderStatus.PREPARING, viewModel.ready().order.status)

        orders.serverStatus[id] = OrderStatus.ON_THE_WAY
        advanceTimeBy(poll)
        runCurrent()
        assertEquals(OrderStatus.ON_THE_WAY, viewModel.ready().order.status)
    }

    @Test
    fun `polling stops once the order is delivered`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, id) = trackNewOrder()
        orders.serverStatus[id] = OrderStatus.DELIVERED
        advanceTimeBy(poll)
        runCurrent()
        assertEquals(OrderStatus.DELIVERED, viewModel.ready().order.status)
        val checks = orders.refreshCount

        advanceTimeBy(poll * 10)
        runCurrent()
        assertEquals("no more network calls after delivery", checks, orders.refreshCount)
    }

    @Test
    fun `offline polls keep the last status and say so, then recover`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, id) = trackNewOrder()
        orders.online = false
        advanceTimeBy(poll)
        runCurrent()
        assertTrue(viewModel.ready().isOffline)
        assertEquals(OrderStatus.PLACED, viewModel.ready().order.status)

        orders.online = true
        orders.serverStatus[id] = OrderStatus.PICKED_UP
        advanceTimeBy(poll)
        runCurrent()
        assertFalse(viewModel.ready().isOffline)
        assertEquals(OrderStatus.PICKED_UP, viewModel.ready().order.status)
    }
}
