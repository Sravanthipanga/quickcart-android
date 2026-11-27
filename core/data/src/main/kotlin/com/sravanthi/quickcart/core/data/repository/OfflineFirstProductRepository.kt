package com.sravanthi.quickcart.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.sravanthi.quickcart.core.data.paging.ProductRemoteMediator
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.asExternalModel
import com.sravanthi.quickcart.core.model.Product
import com.sravanthi.quickcart.core.network.QuickCartApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class OfflineFirstProductRepository @Inject constructor(
    private val api: QuickCartApi,
    private val database: QuickCartDatabase,
    private val clock: Clock,
) : ProductRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun catalog(): Flow<PagingData<Product>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PAGE_SIZE / 2, enablePlaceholders = false),
        remoteMediator = ProductRemoteMediator(api, database, clock),
        pagingSourceFactory = { database.productDao().pagingSource() },
    ).flow.map { page -> page.map { it.asExternalModel() } }

    override fun observeProduct(id: Long): Flow<Product?> =
        database.productDao().observeById(id).map { it?.asExternalModel() }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
