package com.sravanthi.quickcart.core.network.mock

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** What the mock server remembers about each order: enough to report its live status. */
@Serializable
data class StoredOrder(val orderId: String, val placedAtEpochMillis: Long, val etaMaxMinutes: Int)

/** The mock server's order storage. */
interface MockOrderStore {
    fun save(order: StoredOrder)
    fun get(orderId: String): StoredOrder?

    /** The next order number: one more than the highest seen, so numbers keep increasing across restarts. */
    fun nextOrderNumber(first: Int): Int
}

class InMemoryOrderStore : MockOrderStore {
    private val orders = mutableMapOf<String, StoredOrder>()

    @Synchronized
    override fun save(order: StoredOrder) {
        orders[order.orderId] = order
    }

    @Synchronized
    override fun get(orderId: String) = orders[orderId]

    @Synchronized
    override fun nextOrderNumber(first: Int) = nextNumber(orders.keys, first)
}

/** Keeps orders in a JSON file, so the mock server still knows them after the app restarts. */
class FileOrderStore(private val file: File, private val json: Json) : MockOrderStore {

    @Synchronized
    override fun save(order: StoredOrder) {
        val orders = readAll().filterNot { it.orderId == order.orderId } + order
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(orders))
    }

    @Synchronized
    override fun get(orderId: String) = readAll().firstOrNull { it.orderId == orderId }

    @Synchronized
    override fun nextOrderNumber(first: Int) = nextNumber(readAll().map { it.orderId }, first)

    private fun readAll(): List<StoredOrder> =
        if (file.exists()) runCatching { json.decodeFromString<List<StoredOrder>>(file.readText()) }.getOrDefault(emptyList()) else emptyList()
}

private fun nextNumber(ids: Collection<String>, first: Int): Int =
    ((ids.mapNotNull { it.removePrefix(ORDER_PREFIX).toIntOrNull() }.maxOrNull() ?: (first - 1)) + 1).coerceAtLeast(first)

internal const val ORDER_PREFIX = "QC-"
