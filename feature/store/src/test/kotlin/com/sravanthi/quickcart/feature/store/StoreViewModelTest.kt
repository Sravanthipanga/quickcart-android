package com.sravanthi.quickcart.feature.store

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sravanthi.quickcart.core.testing.FakeStoreRepository
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

    // 20:00, when the pizzeria (11:00–23:00) is open.
    private val clock = Clock.fixed(Instant.parse("2026-03-10T20:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(storeId: Long = 1) =
        StoreViewModel(SavedStateHandle(mapOf(STORE_ID_ARG to storeId)), repository, clock)

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
}
