package com.sravanthi.quickcart.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.sravanthi.quickcart.core.data.isRecoverableNetworkError
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.RemoteKeyEntity
import com.sravanthi.quickcart.core.database.model.StoreEntity
import com.sravanthi.quickcart.core.database.model.asEntity
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.model.asExternalModel
import java.time.Clock
import java.time.Duration

/**
 * Loads pages of stores from the marketplace API into Room. The UI only ever reads Room
 * (single source of truth), so whatever was loaded last keeps working offline.
 *
 * It always pages the full store list; type filters (e.g. only groceries) are applied by the
 * Room query, and scrolling to the end of a filtered list simply loads the next page.
 */
@OptIn(ExperimentalPagingApi::class)
class StoreRemoteMediator(
    private val api: MarketplaceApi,
    private val database: QuickCartDatabase,
    private val clock: Clock,
    private val cacheTimeout: Duration = Duration.ofHours(1),
) : RemoteMediator<Int, StoreEntity>() {

    private val storeDao = database.storeDao()
    private val remoteKeyDao = database.remoteKeyDao()

    /** Show the cache immediately when it is fresh, and refresh from the network only when it is stale. */
    override suspend fun initialize(): InitializeAction {
        val key = remoteKeyDao.get(LABEL) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = Duration.ofMillis(clock.millis() - key.updatedAtMillis)
        return if (age < cacheTimeout && storeDao.count() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, StoreEntity>): MediatorResult {
        val skip = when (loadType) {
            LoadType.REFRESH -> 0
            // The list is append-only: new pages always come after the cached ones.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            // No saved key (nothing loaded yet) or no next page: nothing more to append.
            LoadType.APPEND -> remoteKeyDao.get(LABEL)?.nextSkip
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val page = api.getStores(limit = state.config.pageSize, skip = skip)
            val nextSkip = (page.skip + page.stores.size).takeIf { page.stores.isNotEmpty() && it < page.total }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) storeDao.clearAll()
                storeDao.upsertAll(page.stores.mapIndexed { index, store -> store.asExternalModel().asEntity(skip + index) })
                remoteKeyDao.upsert(RemoteKeyEntity(LABEL, nextSkip, clock.millis()))
            }
            MediatorResult.Success(endOfPaginationReached = nextSkip == null)
        } catch (e: Exception) {
            if (e.isRecoverableNetworkError()) MediatorResult.Error(e) else throw e
        }
    }

    companion object {
        const val LABEL = "stores"
    }
}
