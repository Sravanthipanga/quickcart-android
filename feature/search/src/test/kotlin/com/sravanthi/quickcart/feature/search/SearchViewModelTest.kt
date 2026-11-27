package com.sravanthi.quickcart.feature.search

import androidx.lifecycle.SavedStateHandle
import com.sravanthi.quickcart.core.testing.FakeSearchRepository
import com.sravanthi.quickcart.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeSearchRepository()
    // Created inside each test, after the rule has installed the test dispatcher as Main.
    private val viewModel by lazy { SearchViewModel(SavedStateHandle(), repository) }

    @Test
    fun `typing quickly searches only once, after the pause`() = runTest(mainDispatcherRule.testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        listOf("t", "ti", "tir", "tira").forEach { text ->
            viewModel.onQueryChange(text)
            advanceTimeBy(100)
        }
        assertEquals("nothing searched while typing", emptyList<String>(), repository.queries)

        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf("tira"), repository.queries)
        val state = viewModel.uiState.value as SearchUiState.Results
        assertEquals(listOf("Italian Tiramisu"), state.results.items.map { it.item.name })
    }

    @Test
    fun `short queries show suggestions without searching`() = runTest(mainDispatcherRule.testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onQueryChange(" p ")
        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS + 1)
        runCurrent()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `offline results are marked as coming from the cache`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.fromCache = true
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onQueryChange("pizza")
        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS + 1)
        runCurrent()

        assertTrue((viewModel.uiState.value as SearchUiState.Results).fromCache)
    }
}
