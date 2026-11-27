package com.sravanthi.quickcart.feature.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.feature.orders.navigation.ORDER_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Live tracking. While the screen is visible it asks the server for the status every
 * [POLL_INTERVAL_MILLIS] and stops once the order is delivered. The screen always shows the saved
 * order, so it keeps working offline with the last known status.
 */
@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OrderRepository,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle.get<String>(ORDER_ID_ARG))

    val uiState: StateFlow<TrackOrderUiState> = combine(
        repository.observeOrder(orderId),
        pollingUntilDelivered(),
    ) { order, reachedServer ->
        if (order == null) TrackOrderUiState.NotFound else TrackOrderUiState.Ready(order, isOffline = !reachedServer)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackOrderUiState.Loading)

    /**
     * Emits whether each status check reached the server. Runs only while someone collects [uiState],
     * and stops once the order is delivered.
     */
    private fun pollingUntilDelivered(): Flow<Boolean> = flow {
        var lastReached = true
        emit(lastReached)
        while (true) {
            val order = repository.observeOrder(orderId).firstOrNull()
            if (order != null && !order.isActive) return@flow
            lastReached = repository.refreshStatus(orderId)
            emit(lastReached)
            delay(POLL_INTERVAL_MILLIS)
        }
    }

    fun reorder(then: () -> Unit) {
        viewModelScope.launch {
            repository.reorder(orderId)
            then()
        }
    }

    companion object {
        const val POLL_INTERVAL_MILLIS = 15_000L
    }
}

sealed interface TrackOrderUiState {
    data object Loading : TrackOrderUiState
    data object NotFound : TrackOrderUiState

    /** [isOffline]: the last status check failed, so the status shown may be out of date. */
    data class Ready(val order: Order, val isOffline: Boolean) : TrackOrderUiState
}
