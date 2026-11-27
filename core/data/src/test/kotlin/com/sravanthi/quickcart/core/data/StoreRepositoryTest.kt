package com.sravanthi.quickcart.core.data

import com.sravanthi.quickcart.core.data.repository.OfflineFirstStoreRepository
import com.sravanthi.quickcart.core.database.model.MenuItemEntity
import kotlinx.coroutines.flow.first
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoreRepositoryTest {

    private val marketplace = TestMarketplace()
    private val repository = OfflineFirstStoreRepository(marketplace.api, marketplace.database, Clock.systemUTC())

    @After
    fun tearDown() = marketplace.database.close()

    @Test
    fun `refreshing a store caches its details and menu in order`() = runTest {
        assertTrue(repository.refreshStore(1))

        assertEquals("Bella Napoli Pizzeria", repository.observeStore(1).first()?.name)
        val menu = repository.observeMenu(1).first()
        assertEquals(listOf("Pizza", "Pasta", "Starters", "Desserts"), menu.map { it.section }.distinct())
        assertEquals("Classic Margherita Pizza", menu.first().name)
    }

    @Test
    fun `refresh replaces the whole menu, so removed dishes disappear`() = runTest {
        marketplace.database.menuItemDao().insertAll(
            listOf(MenuItemEntity(99, 1, "Discontinued Calzone", "", "Pizza", 1000, 0.0, null, "", null, "", 0)),
        )
        repository.refreshStore(1)
        assertFalse(repository.observeMenu(1).first().any { it.name == "Discontinued Calzone" })
    }

    @Test
    fun `offline refresh fails softly and keeps the cached menu`() = runTest {
        repository.refreshStore(8)
        val cached = repository.observeMenu(8).first()
        marketplace.offline = true

        assertFalse(repository.refreshStore(8))
        assertEquals(cached, repository.observeMenu(8).first())
    }

    @Test
    fun `a store opened before the list loads does not take a place in the list order`() = runTest {
        repository.refreshStore(9)
        assertEquals(Int.MAX_VALUE, marketplace.database.storeDao().positionOf(9))
    }
}
