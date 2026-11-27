package com.sravanthi.quickcart.feature.home

import androidx.paging.testing.asSnapshot
import com.sravanthi.quickcart.core.model.StoreType
import com.sravanthi.quickcart.core.testing.FakeStoreRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.core.testing.sampleStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeStoreRepository()
    private val viewModel = HomeViewModel(repository, Clock.systemUTC())

    // The cached flow never completes, so snapshot the PagingData the screen currently receives.
    private suspend fun currentStoreNames() = flowOf(viewModel.stores.first()).asSnapshot().map { it.name }

    @Test
    fun `all stores are shown by default`() = runTest {
        assertNull(viewModel.selectedType.value)
        assertEquals(sampleStores.map { it.name }, currentStoreNames())
    }

    @Test
    fun `choosing a filter shows only that type of store`() = runTest {
        viewModel.selectType(StoreType.GROCERY)

        assertEquals(StoreType.GROCERY, viewModel.selectedType.value)
        assertEquals(listOf("FreshMart Grocery"), currentStoreNames())
        assertEquals(StoreType.GROCERY, repository.requestedTypes.last())
    }
}
