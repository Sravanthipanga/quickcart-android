package com.sravanthi.quickcart.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.sravanthi.quickcart.core.data.isRecoverableNetworkError
import com.sravanthi.quickcart.core.data.paging.StoreRemoteMediator
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.asEntity
import com.sravanthi.quickcart.core.database.model.asExternalModel
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class OfflineFirstStoreRepository @Inject constructor(
    private val api: MarketplaceApi,
    private val database: QuickCartDatabase,
    private val clock: Clock,
) : StoreRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun stores(type: StoreType?): Flow<PagingData<Store>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PAGE_SIZE / 2, enablePlaceholders = false),
        remoteMediator = StoreRemoteMediator(api, database, clock),
        pagingSourceFactory = { database.storeDao().pagingSource(type?.name) },
    ).flow.map { page -> page.map { it.asExternalModel() } }

    override fun observeStore(id: Long): Flow<Store?> =
        database.storeDao().observeById(id).map { it?.asExternalModel() }

    override fun observeMenu(storeId: Long): Flow<List<MenuItem>> =
        database.menuItemDao().observeForStore(storeId).map { list -> list.map { it.asExternalModel() } }

    override suspend fun refreshStore(id: Long): Boolean = try {
        val (store, menu) = coroutineScope {
            val store = async { api.getStore(id).asExternalModel() }
            val menu = async { api.getMenu(id).items.map { it.asExternalModel() } }
            store.await() to menu.await()
        }
        database.withTransaction {
            val storeDao = database.storeDao()
            // A store opened from search may not be in the paged list yet; keep it out of the list's order.
            storeDao.upsertAll(listOf(store.asEntity(position = storeDao.positionOf(id) ?: Int.MAX_VALUE)))
            database.menuItemDao().replaceForStore(id, menu.mapIndexed { index, item -> item.asEntity(index) })
        }
        true
    } catch (e: Exception) {
        if (e.isRecoverableNetworkError()) false else throw e
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}
