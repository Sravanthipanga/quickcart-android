package com.sravanthi.quickcart.core.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import com.sravanthi.quickcart.core.data.paging.StoreRemoteMediator
import com.sravanthi.quickcart.core.database.model.StoreEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoreRemoteMediatorTest {

    private val marketplace = TestMarketplace()
    private var now = Instant.parse("2026-03-10T12:00:00Z")
    private fun mediator() = StoreRemoteMediator(marketplace.api, marketplace.database, Clock.fixed(now, ZoneOffset.UTC))

    // Pages of 4 over the 11 bundled stores: 4 + 4 + 3.
    private val state = PagingState<Int, StoreEntity>(emptyList(), null, PagingConfig(pageSize = 4), 0)

    @After
    fun tearDown() = marketplace.database.close()

    private fun cachedIds(): List<Long> = marketplace.database.query("SELECT id FROM stores ORDER BY position", null)
        .use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }

    @Test
    fun `refresh caches the first page in marketplace order`() = runTest {
        val result = mediator().load(LoadType.REFRESH, state) as MediatorResult.Success
        assertFalse(result.endOfPaginationReached)
        assertEquals(listOf(1L, 2L, 3L, 4L), cachedIds())
    }

    @Test
    fun `append continues until the last page`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        assertFalse((mediator.load(LoadType.APPEND, state) as MediatorResult.Success).endOfPaginationReached)
        assertTrue((mediator.load(LoadType.APPEND, state) as MediatorResult.Success).endOfPaginationReached)
        assertEquals((1L..11L).toList(), cachedIds())
    }

    @Test
    fun `refresh replaces the cache instead of duplicating it`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)
        mediator.load(LoadType.REFRESH, state)
        assertEquals(listOf(1L, 2L, 3L, 4L), cachedIds())
    }

    @Test
    fun `going offline reports an error and keeps the cached stores`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, state)
        marketplace.offline = true

        assertTrue(mediator.load(LoadType.REFRESH, state) is MediatorResult.Error)
        assertEquals(listOf(1L, 2L, 3L, 4L), cachedIds())
    }

    @Test
    fun `a fresh cache skips the startup refresh, a stale one refreshes`() = runTest {
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
        mediator().load(LoadType.REFRESH, state)

        now = now.plus(Duration.ofMinutes(30))
        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, mediator().initialize())

        now = now.plus(Duration.ofMinutes(31))
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `prepend never calls the network`() = runTest {
        assertTrue((mediator().load(LoadType.PREPEND, state) as MediatorResult.Success).endOfPaginationReached)
        assertEquals(0, marketplace.requests)
    }
}
