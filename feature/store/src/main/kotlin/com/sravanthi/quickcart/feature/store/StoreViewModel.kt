package com.sravanthi.quickcart.feature.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.quickcart.core.data.repository.CartRepository
import com.sravanthi.quickcart.core.data.repository.StoreRepository
import com.sravanthi.quickcart.core.model.AddToCartResult
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.bySection
import com.sravanthi.quickcart.feature.store.navigation.STORE_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

/**
 * Shows the cached store and menu immediately and refreshes them from the network in the background.
 * If the refresh fails, the cached menu stays on screen with an offline notice.
 */
@HiltViewModel
class StoreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StoreRepository,
    private val cartRepository: CartRepository,
    private val clock: Clock,
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle.get<Long>(STORE_ID_ARG))
    private val refresh = MutableStateFlow(RefreshState.Refreshing)
    private val pendingSwitch = MutableStateFlow<CartSwitch?>(null)

    val uiState: StateFlow<StoreUiState> = combine(
        repository.observeStore(storeId),
        repository.observeMenu(storeId),
        refresh,
        cartRepository.observeCart(),
        pendingSwitch,
    ) { store, menu, refreshState, cart, switch ->
        when {
            store != null -> StoreUiState.Ready(
                store = store,
                sections = menu.bySection(),
                isOpen = store.isOpenAt(LocalTime.now(clock)),
                isRefreshing = refreshState == RefreshState.Refreshing,
                isOffline = refreshState == RefreshState.Failed,
                cart = cart.takeIf { it.store?.id == storeId } ?: Cart.Empty,
                pendingSwitch = switch,
            )
            refreshState == RefreshState.Failed -> StoreUiState.Error
            else -> StoreUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoreUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        refresh.value = RefreshState.Refreshing
        viewModelScope.launch {
            refresh.value = if (repository.refreshStore(storeId)) RefreshState.Done else RefreshState.Failed
        }
    }

    fun add(item: MenuItem) {
        viewModelScope.launch {
            val result = cartRepository.add(item)
            if (result is AddToCartResult.DifferentStore) pendingSwitch.value = CartSwitch(item, result.currentStoreName)
        }
    }

    fun setQuantity(item: MenuItem, quantity: Int) {
        viewModelScope.launch { cartRepository.setQuantity(item.id, quantity) }
    }

    /** The user agreed to empty their other cart and start one here. */
    fun confirmSwitch() {
        val switch = pendingSwitch.value ?: return
        pendingSwitch.value = null
        viewModelScope.launch { cartRepository.startNewCartWith(switch.item) }
    }

    fun dismissSwitch() {
        pendingSwitch.value = null
    }

    private enum class RefreshState { Refreshing, Done, Failed }
}

/** Waiting for the user to confirm replacing a cart from [currentStoreName] with [item]. */
data class CartSwitch(val item: MenuItem, val currentStoreName: String)

sealed interface StoreUiState {
    data object Loading : StoreUiState

    /** Nothing cached and the network failed. */
    data object Error : StoreUiState

    data class Ready(
        val store: Store,
        val sections: Map<String, List<MenuItem>>,
        val isOpen: Boolean,
        val isRefreshing: Boolean,
        /** The refresh failed, so this is the cached menu. */
        val isOffline: Boolean,
        /** The cart, if it belongs to this store; empty otherwise. */
        val cart: Cart = Cart.Empty,
        val pendingSwitch: CartSwitch? = null,
    ) : StoreUiState
}
