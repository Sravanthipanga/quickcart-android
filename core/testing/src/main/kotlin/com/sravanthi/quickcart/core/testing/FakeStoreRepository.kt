package com.sravanthi.quickcart.core.testing

import androidx.paging.PagingData
import com.sravanthi.quickcart.core.data.repository.StoreRepository
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * In-memory [StoreRepository]. [refreshStore] copies the "server" store and menu into the cache
 * unless [online] is false.
 */
class FakeStoreRepository(
    private val serverStores: List<Store> = sampleStores,
    private val serverMenus: Map<Long, List<MenuItem>> = mapOf(1L to sampleMenu, 8L to sampleGroceries),
) : StoreRepository {

    var online = true
    val requestedTypes = mutableListOf<StoreType?>()

    private val cachedStores = MutableStateFlow<Map<Long, Store>>(emptyMap())
    private val cachedMenus = MutableStateFlow<Map<Long, List<MenuItem>>>(emptyMap())

    override fun stores(type: StoreType?): Flow<PagingData<Store>> {
        requestedTypes += type
        return flowOf(PagingData.from(serverStores.filter { type == null || it.type == type }))
    }

    override fun observeStore(id: Long): Flow<Store?> = cachedStores.map { it[id] }

    override fun observeMenu(storeId: Long): Flow<List<MenuItem>> = cachedMenus.map { it[storeId].orEmpty() }

    override suspend fun refreshStore(id: Long): Boolean {
        if (!online) return false
        val store = serverStores.firstOrNull { it.id == id } ?: return false
        cachedStores.value = cachedStores.value + (id to store)
        cachedMenus.value = cachedMenus.value + (id to serverMenus[id].orEmpty())
        return true
    }
}
