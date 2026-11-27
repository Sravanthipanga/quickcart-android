package com.sravanthi.quickcart.feature.catalog

import androidx.paging.testing.asSnapshot
import com.sravanthi.quickcart.core.testing.FakeProductRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import com.sravanthi.quickcart.core.testing.sampleProducts
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `exposes the repository catalog in order`() = runTest {
        val viewModel = CatalogViewModel(FakeProductRepository())

        // The cached flow never completes, so snapshot the PagingData the screen currently receives.
        val current = viewModel.products.first()

        assertEquals(sampleProducts.map { it.id }, flowOf(current).asSnapshot().map { it.id })
    }
}
