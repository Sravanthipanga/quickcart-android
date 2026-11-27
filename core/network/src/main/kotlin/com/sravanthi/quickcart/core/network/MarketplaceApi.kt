package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.model.NetworkMenu
import com.sravanthi.quickcart.core.network.model.NetworkSearchResults
import com.sravanthi.quickcart.core.network.model.NetworkStore
import com.sravanthi.quickcart.core.network.model.NetworkStorePage
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** The QuickCart marketplace API. Pages use limit/skip offsets. */
interface MarketplaceApi {

    @GET("stores")
    suspend fun getStores(@Query("limit") limit: Int, @Query("skip") skip: Int): NetworkStorePage

    @GET("stores/{id}")
    suspend fun getStore(@Path("id") id: Long): NetworkStore

    @GET("stores/{id}/items")
    suspend fun getMenu(@Path("id") storeId: Long): NetworkMenu

    @GET("search")
    suspend fun search(@Query("q") query: String): NetworkSearchResults
}
