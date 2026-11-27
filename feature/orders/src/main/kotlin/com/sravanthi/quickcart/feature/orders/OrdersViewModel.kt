package com.sravanthi.quickcart.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<OrdersUiState> = repository.observeOrders()
        .map { orders ->
            if (orders.isEmpty()) {
                OrdersUiState.Empty
            } else {
                OrdersUiState.Ready(active = orders.filter { it.isActive }, past = orders.filterNot { it.isActive })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState.Loading)

    /** Refills the cart from a past order; the screen then opens the cart. */
    fun reorder(orderId: String, then: () -> Unit) {
        viewModelScope.launch {
            repository.reorder(orderId)
            then()
        }
    }
}

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Ready(val active: List<Order>, val past: List<Order>) : OrdersUiState
}
