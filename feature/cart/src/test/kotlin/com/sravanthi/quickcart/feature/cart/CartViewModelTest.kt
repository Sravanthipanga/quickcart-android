package com.sravanthi.quickcart.feature.cart

import app.cash.turbine.test
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeOrderRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.core.testing.sampleMenu
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = FakeCartRepository()
    private val orders = FakeOrderRepository(cart)
    private var clock = Clock.fixed(Instant.parse("2026-03-10T19:00:00Z"), ZoneOffset.UTC) // pizzeria open 11–23

    private val viewModel by lazy { CartViewModel(cart, orders, clock) }

    private val pizza = sampleMenu[0] // $14.99
    private val bruschetta = sampleMenu[4] // $8.99

    @Test
    fun `an empty cart shows the empty state`() = runTest {
        viewModel.uiState.test { assertEquals(CartUiState.Empty, expectMostRecentItem()) }
    }

    @Test
    fun `the summary prices the cart and allows ordering`() = runTest {
        cart.add(pizza)
        cart.add(pizza)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as CartUiState.Ready
            assertEquals(2998L, state.pricing.subtotalCents)
            assertEquals(199L, state.pricing.deliveryFeeCents)
            assertEquals(150L, state.pricing.serviceFeeCents)
            assertTrue(state.canPlaceOrder)
        }
    }

    @Test
    fun `below the minimum ordering is disabled`() = runTest {
        cart.add(bruschetta) // $8.99 < $10 minimum in the sample store
        viewModel.uiState.test {
            val state = expectMostRecentItem() as CartUiState.Ready
            assertFalse(state.pricing.meetsMinimum)
            assertFalse(state.canPlaceOrder)
        }
    }

    @Test
    fun `a closed store cannot take the order`() = runTest {
        clock = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneOffset.UTC)
        cart.add(pizza)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as CartUiState.Ready
            assertFalse(state.isStoreOpen)
            assertFalse(state.canPlaceOrder)
        }
    }

    @Test
    fun `placing an order shows the confirmation`() = runTest {
        cart.add(pizza)
        viewModel.uiState.test {
            viewModel.placeOrder()
            val placed = expectMostRecentItem() as CartUiState.Placed
            assertEquals("Bella Napoli Pizzeria", placed.confirmation.storeName)
            assertEquals(1, orders.placedCount)
        }
    }

    @Test
    fun `a rejection shows the server's message and keeps the cart`() = runTest {
        cart.add(pizza)
        orders.nextResult = PlaceOrderResult.Rejected("An item is no longer available")
        viewModel.uiState.test {
            viewModel.placeOrder()
            val state = expectMostRecentItem() as CartUiState.Ready
            assertEquals(CheckoutError.Rejected("An item is no longer available"), state.error)
            assertEquals(1, state.cart.itemCount)
        }
    }

    @Test
    fun `offline checkout says so, and changing the cart clears the message`() = runTest {
        cart.add(pizza)
        orders.nextResult = PlaceOrderResult.Offline
        viewModel.uiState.test {
            viewModel.placeOrder()
            assertEquals(CheckoutError.Offline, (expectMostRecentItem() as CartUiState.Ready).error)

            viewModel.setQuantity(pizza.id, 2)
            assertEquals(null, (expectMostRecentItem() as CartUiState.Ready).error)
        }
    }
}
