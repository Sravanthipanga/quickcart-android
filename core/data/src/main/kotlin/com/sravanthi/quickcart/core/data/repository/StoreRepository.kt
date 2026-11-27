package com.sravanthi.quickcart.core.data.repository

import androidx.paging.PagingData
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    /** Stores on the marketplace, optionally only one [type], paged and cached for offline use. */
    fun stores(type: StoreType? = null): Flow<PagingData<Store>>

    fun observeStore(id: Long): Flow<Store?>

    /** A store's menu or aisles from the cache, in order. */
    fun observeMenu(storeId: Long): Flow<List<MenuItem>>

    /**
     * Fetches the latest store details and menu into the cache.
     * Returns false if the network failed; the cached menu (if any) stays available.
     */
    suspend fun refreshStore(id: Long): Boolean
}
