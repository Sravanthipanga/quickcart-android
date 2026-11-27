package com.sravanthi.quickcart.feature.store

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeStoreRepository
import com.sravanthi.quickcart.core.testing.sampleGroceries
import com.sravanthi.quickcart.core.testing.sampleMenu
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.feature.store.navigation.STORE_ID_ARG
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeStoreRepository()
    private val cart = FakeCartRepository()

    // 20:00, when the pizzeria (11:00–23:00) is open.
    private val clock = Clock.fixed(Instant.parse("2026-03-10T20:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(storeId: Long = 1) =
        StoreViewModel(SavedStateHandle(mapOf(STORE_ID_ARG to storeId)), repository, cart, clock)

    @Test
    fun `shows the store and its menu grouped by section`() = runTest {
        viewModel().uiState.test {
            val state = expectMostRecentItem() as StoreUiState.Ready
            assertEquals("Bella Napoli Pizzeria", state.store.name)
            assertEquals(listOf("Pizza", "Pasta", "Starters", "Desserts"), state.sections.keys.toList())
            assertTrue(state.isOpen)
            assertFalse(state.isOffline)
        }
    }

    @Test
    fun `offline with nothing cached shows an error, and retry recovers`() = runTest {
        repository.online = false
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(StoreUiState.Error, expectMostRecentItem())

            repository.online = true
            viewModel.refresh()
            assertTrue(expectMostRecentItem() is StoreUiState.Ready)
        }
    }

    @Test
    fun `offline with a cached menu keeps showing it with an offline notice`() = runTest {
        repository.refreshStore(1)
        repository.online = false

        viewModel().uiState.test {
            val state = expectMostRecentItem() as StoreUiState.Ready
            assertTrue(state.isOffline)
            assertEquals(6, state.sections.values.sumOf { it.size })
        }
    }

    @Test
    fun `adding items fills this store's cart and shows quantities`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            viewModel.add(sampleMenu[0])
            viewModel.add(sampleMenu[0])
            viewModel.setQuantity(sampleMenu[0], 3)

            val state = expectMostRecentItem() as StoreUiState.Ready
            assertEquals(3, state.cart.quantityOf(sampleMenu[0].id))
            assertEquals(1499L * 3, state.cart.subtotalCents)
        }
    }

    @Test
    fun `adding from another store asks before replacing the cart`() = runTest {
        cart.add(sampleGroceries[0]) // the cart now belongs to FreshMart
        val viewModel = viewModel(storeId = 1)
        viewModel.uiState.test {
            viewModel.add(sampleMenu[0])
            val asking = expectMostRecentItem() as StoreUiState.Ready
            assertEquals("FreshMart Grocery", asking.pendingSwitch?.currentStoreName)
            assertTrue("another store's cart is not shown here", asking.cart.isEmpty)

            viewModel.confirmSwitch()
            val switched = expectMostRecentItem() as StoreUiState.Ready
            assertEquals(null, switched.pendingSwitch)
            assertEquals(1, switched.cart.quantityOf(sampleMenu[0].id))
        }
    }

    @Test
    fun `keeping the old cart leaves it untouched`() = runTest {
        cart.add(sampleGroceries[0])
        val viewModel = viewModel(storeId = 1)
        viewModel.uiState.test {
            viewModel.add(sampleMenu[0])
            viewModel.dismissSwitch()
            assertEquals(null, (expectMostRecentItem() as StoreUiState.Ready).pendingSwitch)
            assertEquals("FreshMart Grocery", cart.cart.value.store?.name)
        }
    }
}
