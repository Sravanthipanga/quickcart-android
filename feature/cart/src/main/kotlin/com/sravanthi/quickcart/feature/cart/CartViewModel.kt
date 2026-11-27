package com.sravanthi.quickcart.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.quickcart.core.data.repository.CartRepository
import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.OrderConfirmation
import com.sravanthi.quickcart.core.model.OrderPricing
import com.sravanthi.quickcart.core.model.priceOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val clock: Clock,
) : ViewModel() {

    private val checkout = MutableStateFlow(CheckoutState())

    val uiState: StateFlow<CartUiState> = combine(cartRepository.observeCart(), checkout) { cart, checkoutState ->
        val store = cart.store
        when {
            checkoutState.confirmation != null -> CartUiState.Placed(checkoutState.confirmation)
            cart.isEmpty || store == null -> CartUiState.Empty
            else -> {
                val pricing = priceOrder(cart.subtotalCents, store)
                val isOpen = store.isOpenAt(LocalTime.now(clock))
                CartUiState.Ready(
                    cart = cart,
                    pricing = pricing,
                    isStoreOpen = isOpen,
                    isPlacing = checkoutState.isPlacing,
                    error = checkoutState.error,
                    canPlaceOrder = pricing.meetsMinimum && isOpen && !checkoutState.isPlacing,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState.Loading)

    fun setQuantity(itemId: Long, quantity: Int) {
        checkout.update { it.copy(error = null) }
        viewModelScope.launch { cartRepository.setQuantity(itemId, quantity) }
    }

    fun placeOrder() {
        if (checkout.value.isPlacing) return
        checkout.update { it.copy(isPlacing = true, error = null) }
        viewModelScope.launch {
            checkout.value = when (val result = orderRepository.placeOrder()) {
                is PlaceOrderResult.Placed -> CheckoutState(confirmation = result.confirmation)
                is PlaceOrderResult.Rejected -> CheckoutState(error = CheckoutError.Rejected(result.message))
                PlaceOrderResult.Offline -> CheckoutState(error = CheckoutError.Offline)
            }
        }
    }

    private data class CheckoutState(
        val isPlacing: Boolean = false,
        val error: CheckoutError? = null,
        val confirmation: OrderConfirmation? = null,
    )
}

sealed interface CheckoutError {
    data class Rejected(val message: String) : CheckoutError
    data object Offline : CheckoutError
}

sealed interface CartUiState {
    data object Loading : CartUiState
    data object Empty : CartUiState

    data class Ready(
        val cart: Cart,
        val pricing: OrderPricing,
        val isStoreOpen: Boolean,
        val isPlacing: Boolean,
        val error: CheckoutError?,
        val canPlaceOrder: Boolean,
    ) : CartUiState

    data class Placed(val confirmation: OrderConfirmation) : CartUiState
}
