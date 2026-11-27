package com.sravanthi.quickcart.core.data.repository

import androidx.paging.PagingData
import com.sravanthi.quickcart.core.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** The full catalog, paged from the network and cached for offline use. */
    fun catalog(): Flow<PagingData<Product>>

    fun observeProduct(id: Long): Flow<Product?>
}
