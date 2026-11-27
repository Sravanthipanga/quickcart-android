package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.data.isRecoverableNetworkError
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.asExternalModel
import com.sravanthi.quickcart.core.model.SearchItem
import com.sravanthi.quickcart.core.model.SearchResults
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.model.asExternalModel
import javax.inject.Inject

internal class OfflineFirstSearchRepository @Inject constructor(
    private val api: MarketplaceApi,
    private val database: QuickCartDatabase,
) : SearchRepository {

    override suspend fun search(query: String): SearchOutcome {
        val q = query.trim()
        if (q.length < MIN_QUERY_LENGTH) return SearchOutcome(SearchResults.Empty, fromCache = false)

        return try {
            val response = api.search(q)
            SearchOutcome(
                SearchResults(
                    stores = response.stores.map { it.asExternalModel() },
                    items = response.items.map { SearchItem(it.item.asExternalModel(), it.storeName) },
                ),
                fromCache = false,
            )
        } catch (e: Exception) {
            if (!e.isRecoverableNetworkError()) throw e
            SearchOutcome(searchCache(q), fromCache = true)
        }
    }

    private suspend fun searchCache(query: String): SearchResults {
        val stores = database.storeDao().search(query).map { it.asExternalModel() }
        val items = database.menuItemDao().search(query, MAX_ITEMS).map { it.asExternalModel() }
        val names = database.storeDao().namesFor(items.map { it.storeId }.distinct()).associate { it.id to it.name }
        return SearchResults(stores, items.map { SearchItem(it, names[it.storeId].orEmpty()) })
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_ITEMS = 40
    }
}
