package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.model.NetworkProduct
import com.sravanthi.quickcart.core.network.model.NetworkProductPage
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** The product catalog API (DummyJSON). Pages use limit/skip offsets. */
interface QuickCartApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): NetworkProductPage

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): NetworkProductPage

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Long): NetworkProduct
}
