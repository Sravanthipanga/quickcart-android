package com.sravanthi.quickcart.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sravanthi.quickcart.core.data.repository.ProductRepository
import com.sravanthi.quickcart.core.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    repository: ProductRepository,
) : ViewModel() {
    /** Cached in the ViewModel so rotation keeps the loaded pages and scroll position. */
    val products: Flow<PagingData<Product>> = repository.catalog().cachedIn(viewModelScope)
}
