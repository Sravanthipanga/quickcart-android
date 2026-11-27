package com.sravanthi.quickcart.core.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sravanthi.quickcart.core.data.paging.ProductRemoteMediator
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.ProductEntity
import com.sravanthi.quickcart.core.network.QuickCartApi
import com.sravanthi.quickcart.core.network.model.NetworkProduct
import com.sravanthi.quickcart.core.network.model.NetworkProductPage
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductRemoteMediatorTest {

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        QuickCartDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val api = FakeApi(total = 45)
    private var now = Instant.parse("2026-03-10T12:00:00Z")
    private val clock get() = Clock.fixed(now, ZoneOffset.UTC)

    private fun mediator() = ProductRemoteMediator(api, database, clock, cacheTimeout = Duration.ofHours(1))
    private val state = PagingState<Int, ProductEntity>(emptyList(), null, PagingConfig(pageSize = 20), 0)

    @After
    fun tearDown() = database.close()

    private fun storedIds(): List<Long> =
        database.query("SELECT id FROM products ORDER BY position", null)
            .use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }

    @Test
    fun `refresh stores the first page in API order`() = runTest {
        val result = mediator().load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals((1L..20L).toList(), storedIds())
        assertEquals(20, database.remoteKeyDao().get(ProductRemoteMediator.LABEL)?.nextSkip)
    }

    @Test
    fun `append continues from the saved key until the last page`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        val second = mediator.load(LoadType.APPEND, state) as RemoteMediator.MediatorResult.Success
        val third = mediator.load(LoadType.APPEND, state) as RemoteMediator.MediatorResult.Success
        val fourth = mediator.load(LoadType.APPEND, state) as RemoteMediator.MediatorResult.Success

        assertEquals(listOf(0, 20, 40), api.requestedSkips)
        assertFalse(second.endOfPaginationReached)
        assertTrue("45 products: the third page is the last", third.endOfPaginationReached)
        assertTrue(fourth.endOfPaginationReached)
        assertEquals((1L..45L).toList(), storedIds())
    }

    @Test
    fun `refresh replaces the cache instead of appending duplicates`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)
        mediator.load(LoadType.REFRESH, state)

        assertEquals((1L..20L).toList(), storedIds())
    }

    @Test
    fun `network failure is reported and the cache is kept`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        api.failNext = true

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals("offline users still see the last catalog", 20, storedIds().size)
    }

    @Test
    fun `a fresh cache skips the startup refresh, a stale one refreshes`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())

        mediator().load(LoadType.REFRESH, state)
        now = now.plus(Duration.ofMinutes(30))
        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator().initialize())

        now = now.plus(Duration.ofMinutes(31))
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `prepend never loads`() = runTest {
        val result = mediator().load(LoadType.PREPEND, state) as RemoteMediator.MediatorResult.Success
        assertTrue(result.endOfPaginationReached)
        assertEquals(emptyList<Int>(), api.requestedSkips)
    }
}

/** Serves products 1..[total] in pages, like the real API. */
private class FakeApi(private val total: Int) : QuickCartApi {
    val requestedSkips = mutableListOf<Int>()
    var failNext = false

    override suspend fun getProducts(limit: Int, skip: Int): NetworkProductPage {
        if (failNext) {
            failNext = false
            throw IOException("offline")
        }
        requestedSkips += skip
        val ids = (skip + 1..minOf(skip + limit, total)).map { it.toLong() }
        return NetworkProductPage(ids.map { NetworkProduct(id = it, title = "Product $it", price = 9.99) }, total, skip, limit)
    }

    override suspend fun searchProducts(query: String, limit: Int, skip: Int) = error("unused")
    override suspend fun getProduct(id: Long) = error("unused")
}
