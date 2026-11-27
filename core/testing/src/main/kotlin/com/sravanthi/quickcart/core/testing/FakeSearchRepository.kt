package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.data.repository.SearchOutcome
import com.sravanthi.quickcart.core.data.repository.SearchRepository
import com.sravanthi.quickcart.core.model.SearchItem
import com.sravanthi.quickcart.core.model.SearchResults

/** Matches sample stores and items by name, and records every query it receives. */
class FakeSearchRepository(
    private val stores: List<com.sravanthi.quickcart.core.model.Store> = sampleStores,
    private val items: List<com.sravanthi.quickcart.core.model.MenuItem> = sampleMenu + sampleGroceries,
) : SearchRepository {

    val queries = mutableListOf<String>()
    var fromCache = false

    override suspend fun search(query: String): SearchOutcome {
        queries += query
        val q = query.trim()
        if (q.length < 2) return SearchOutcome(SearchResults.Empty, fromCache = false)
        val names = stores.associate { it.id to it.name }
        return SearchOutcome(
            SearchResults(
                stores = stores.filter { it.name.contains(q, true) || it.cuisine.contains(q, true) },
                items = items.filter { it.name.contains(q, true) }.map { SearchItem(it, names[it.storeId].orEmpty()) },
            ),
            fromCache = fromCache,
        )
    }
}
