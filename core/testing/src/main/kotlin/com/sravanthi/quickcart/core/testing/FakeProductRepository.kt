package com.sravanthi.quickcart.core.testing

import androidx.paging.PagingData
import com.sravanthi.quickcart.core.data.repository.ProductRepository
import com.sravanthi.quickcart.core.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeProductRepository(products: List<Product> = sampleProducts) : ProductRepository {
    private val products = MutableStateFlow(products)

    override fun catalog(): Flow<PagingData<Product>> = flowOf(PagingData.from(products.value))

    override fun observeProduct(id: Long): Flow<Product?> = products.map { list -> list.firstOrNull { it.id == id } }
}
