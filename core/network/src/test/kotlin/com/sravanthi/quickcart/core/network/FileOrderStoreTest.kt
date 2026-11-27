package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.mock.FileOrderStore
import com.sravanthi.quickcart.core.network.mock.StoredOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileOrderStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val json = NetworkModule.provideJson()

    @Test
    fun `orders and numbering survive a restart`() {
        val file = folder.root.resolve("mock-server/orders.json")
        FileOrderStore(file, json).apply {
            assertEquals(1042, nextOrderNumber(first = 1042))
            save(StoredOrder("QC-1042", 1_000, 35))
            save(StoredOrder("QC-1043", 2_000, 45))
        }

        // A new store over the same file, as after an app restart.
        val reopened = FileOrderStore(file, json)
        assertEquals(StoredOrder("QC-1043", 2_000, 45), reopened.get("QC-1043"))
        assertEquals(1044, reopened.nextOrderNumber(first = 1042))
        assertNull(reopened.get("QC-1"))
    }

    @Test
    fun `a corrupt file is treated as empty rather than crashing`() {
        val file = folder.newFile("orders.json").apply { writeText("not json") }
        val store = FileOrderStore(file, json)
        assertNull(store.get("QC-1042"))
        assertEquals(1042, store.nextOrderNumber(first = 1042))
    }
}
