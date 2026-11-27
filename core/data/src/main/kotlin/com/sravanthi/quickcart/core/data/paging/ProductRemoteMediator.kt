package com.sravanthi.quickcart.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.ProductEntity
import com.sravanthi.quickcart.core.database.model.RemoteKeyEntity
import com.sravanthi.quickcart.core.database.model.asEntity
import com.sravanthi.quickcart.core.network.QuickCartApi
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.Duration

/**
 * Loads catalog pages from the API into Room. The UI only ever reads Room (single source of truth),
 * so whatever was loaded last keeps working offline.
 */
@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val api: QuickCartApi,
    private val database: QuickCartDatabase,
    private val clock: Clock,
    private val cacheTimeout: Duration = Duration.ofHours(1),
) : RemoteMediator<Int, ProductEntity>() {

    private val productDao = database.productDao()
    private val remoteKeyDao = database.remoteKeyDao()

    /** Show the cache immediately when it is fresh, and refresh from the network only when it is stale. */
    override suspend fun initialize(): InitializeAction {
        val key = remoteKeyDao.get(LABEL) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = Duration.ofMillis(clock.millis() - key.updatedAtMillis)
        return if (age < cacheTimeout && productDao.count() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ProductEntity>): MediatorResult {
        val skip = when (loadType) {
            LoadType.REFRESH -> 0
            // The list is append-only: new pages always come after the cached ones.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            // No saved key (nothing loaded yet) or no next page: nothing more to append.
            LoadType.APPEND -> remoteKeyDao.get(LABEL)?.nextSkip
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val page = api.getProducts(limit = state.config.pageSize, skip = skip)
            val nextSkip = (page.skip + page.products.size).takeIf { page.products.isNotEmpty() && it < page.total }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) productDao.clearAll()
                productDao.upsertAll(page.products.mapIndexed { index, product -> product.asExternalModel().asEntity(skip + index) })
                remoteKeyDao.upsert(RemoteKeyEntity(LABEL, nextSkip, clock.millis()))
            }
            MediatorResult.Success(endOfPaginationReached = nextSkip == null)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: SerializationException) {
            MediatorResult.Error(e)
        }
    }

    companion object {
        const val LABEL = "catalog"
    }
}
