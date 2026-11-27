package com.sravanthi.quickcart.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.quickcart.core.data.repository.SearchRepository
import com.sravanthi.quickcart.core.model.SearchResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: SearchRepository,
) : ViewModel() {

    /** Survives process death, so the user's query is still there when they come back. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    /**
     * Waits until typing pauses before searching, ignores repeats of the same query,
     * and cancels a slow search as soon as the query changes, so results never arrive out of order.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = query
        .map { it.trim() }
        .debounce(DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .transformLatest { q ->
            if (q.length < MIN_QUERY_LENGTH) {
                emit(SearchUiState.Idle)
            } else {
                emit(SearchUiState.Loading)
                val outcome = repository.search(q)
                emit(SearchUiState.Results(q, outcome.results, outcome.fromCache))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)

    fun onQueryChange(value: String) {
        savedStateHandle[QUERY_KEY] = value
    }

    companion object {
        const val DEBOUNCE_MILLIS = 300L
        const val MIN_QUERY_LENGTH = 2
        private const val QUERY_KEY = "query"
    }
}

sealed interface SearchUiState {
    /** Nothing typed yet (or too short): show suggestions. */
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val query: String, val results: SearchResults, val fromCache: Boolean) : SearchUiState
}
