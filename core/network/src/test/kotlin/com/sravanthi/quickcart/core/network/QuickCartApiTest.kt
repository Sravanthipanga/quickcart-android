package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException

class QuickCartApiTest {

    private val server = MockWebServer()
    private val api = NetworkModule.createApi(server.url("/").toString(), OkHttpClient(), NetworkModule.provideJson())

    @After
    fun tearDown() = server.shutdown()

    private fun fixture(name: String) = javaClass.classLoader!!.getResource(name)!!.readText()

    @Test
    fun `products page parses, ignoring unknown fields and defaulting missing ones`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("products_page.json")))

        val page = api.getProducts(limit = 2, skip = 0)

        assertEquals(194, page.total)
        assertEquals(listOf(1L, 2L), page.products.map { it.id })
        val second = page.products[1].asExternalModel()
        assertEquals("", second.description)
        assertNull(second.brand)
        assertEquals(0, second.stock)
    }

    @Test
    fun `paging and search parameters are sent`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("products_page.json")))
        server.enqueue(MockResponse().setBody(fixture("products_page.json")))

        api.getProducts(limit = 20, skip = 40)
        api.searchProducts(query = "phone case", limit = 20, skip = 0)

        assertEquals("/products?limit=20&skip=40", server.takeRequest().path)
        assertEquals("/products/search?q=phone%20case&limit=20&skip=0", server.takeRequest().path)
    }

    @Test
    fun `dollar prices become exact cents`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("products_page.json")))
        val product = api.getProducts(2, 0).products.first().asExternalModel()
        assertEquals(999L, product.priceCents)
        assertEquals("Essence", product.brand)
    }

    @Test(expected = HttpException::class)
    fun `server errors surface as HttpException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        api.getProducts(20, 0)
    }
}
