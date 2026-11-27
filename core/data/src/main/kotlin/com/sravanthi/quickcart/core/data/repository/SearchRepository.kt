package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.model.SearchResults

interface SearchRepository {
    /**
     * Searches stores and items across the whole marketplace. When the network is unavailable,
     * falls back to stores and menus already cached on the device.
     */
    suspend fun search(query: String): SearchOutcome
}

data class SearchOutcome(
    val results: SearchResults,
    /** True when the results came from the device cache because the network failed. */
    val fromCache: Boolean,
)
