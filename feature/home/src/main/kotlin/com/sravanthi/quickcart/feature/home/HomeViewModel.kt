package com.sravanthi.quickcart.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sravanthi.quickcart.core.data.repository.CartRepository
import com.sravanthi.quickcart.core.data.repository.StoreRepository
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StoreRepository,
    cartRepository: CartRepository,
    private val clock: Clock,
) : ViewModel() {

    /** Shown as a badge on the cart button. */
    val cartItemCount: StateFlow<Int> = cartRepository.observeCart()
        .map { it.itemCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)


    private val _selectedType = MutableStateFlow<StoreType?>(null)

    /** The chosen filter chip, or null for "All". */
    val selectedType: StateFlow<StoreType?> = _selectedType.asStateFlow()

    /** Switches pagers when the filter changes; cached so rotation keeps pages and scroll position. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val stores: Flow<PagingData<Store>> = _selectedType
        .flatMapLatest { repository.stores(it) }
        .cachedIn(viewModelScope)

    fun selectType(type: StoreType?) {
        _selectedType.value = type
    }

    /** Used to show which stores are open right now. */
    fun currentTime(): LocalTime = LocalTime.now(clock)
}
