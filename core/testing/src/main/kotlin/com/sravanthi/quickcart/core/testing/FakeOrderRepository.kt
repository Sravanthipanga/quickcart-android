package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.model.CartLine
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderConfirmation
import com.sravanthi.quickcart.core.model.OrderLine
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.model.priceOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/**
 * Accepts orders from [cart] unless [nextResult] says otherwise, keeps them in memory, and reports
 * whatever status is set in [serverStatus] when refreshed (or fails while [online] is false).
 */
class FakeOrderRepository(private val cart: FakeCartRepository) : OrderRepository {
    var nextResult: PlaceOrderResult? = null
    var online = true
    val serverStatus = mutableMapOf<String, OrderStatus>()
    var refreshCount = 0
        private set
    var placedCount = 0
        private set

    val orders = MutableStateFlow<List<Order>>(emptyList())

    override suspend fun placeOrder(): PlaceOrderResult {
        nextResult?.let { return it }
        val current = cart.cart.value
        val store = checkNotNull(current.store)
        placedCount++
        val confirmation = OrderConfirmation(
            "QC-${1041 + placedCount}", store.name, priceOrder(current.subtotalCents, store), 25, 35, PLACED_AT,
        )
        orders.update { listOf(current.toOrder(confirmation)) + it }
        cart.clear()
        return PlaceOrderResult.Placed(confirmation)
    }

    override fun observeOrders(): Flow<List<Order>> = orders

    override fun observeOrder(orderId: String): Flow<Order?> = orders.map { list -> list.firstOrNull { it.id == orderId } }

    override suspend fun refreshStatus(orderId: String): Boolean {
        refreshCount++
        if (!online) return false
        val status = serverStatus[orderId] ?: return true
        orders.update { list -> list.map { if (it.id == orderId) it.copy(status = status) else it } }
        return true
    }

    override suspend fun reorder(orderId: String) {
        val order = orders.value.first { it.id == orderId }
        cart.clear()
        order.lines.forEach { line ->
            repeat(line.quantity) {
                cart.add(MenuItem(line.itemId, order.storeId, line.name, "", "", line.unitPriceCents, 0.0, line.unit, line.imageUrl, null, emptySet()))
            }
        }
    }

    private fun com.sravanthi.quickcart.core.model.Cart.toOrder(confirmation: OrderConfirmation): Order {
        val store = checkNotNull(store)
        return Order(
        id = confirmation.orderId,
        storeId = store.id,
        storeName = store.name,
        storeImageUrl = store.imageUrl,
        lines = lines.map(CartLine::toOrderLine),
        pricing = confirmation.pricing,
        placedAt = confirmation.placedAt,
        etaMinMinutes = confirmation.etaMinMinutes,
        etaMaxMinutes = confirmation.etaMaxMinutes,
        status = OrderStatus.PLACED,
        )
    }

    companion object {
        val PLACED_AT: Instant = Instant.parse("2026-03-10T19:00:00Z")
    }
}

private fun CartLine.toOrderLine() = OrderLine(item.id, item.name, quantity, item.salePriceCents, item.unit, item.imageUrl)

/** Sample orders for previews and screenshots: one on the way, two delivered. */
fun sampleOrders(): List<Order> {
    val pizzeria = sampleStores.first { it.id == 1L }
    val grocery = sampleStores.first { it.id == 8L }
    val ramen = sampleStores.first { it.id == 3L }
    fun order(id: String, store: com.sravanthi.quickcart.core.model.Store, lines: List<CartLine>, placed: String, status: OrderStatus): Order {
        val subtotal = lines.sumOf { it.totalCents }
        return Order(
            id, store.id, store.name, store.imageUrl, lines.map(CartLine::toOrderLine), priceOrder(subtotal, store),
            Instant.parse(placed), store.deliveryMinMinutes, store.deliveryMaxMinutes, status,
        )
    }
    return listOf(
        order("QC-1046", pizzeria, listOf(CartLine(sampleMenu[0], 2), CartLine(sampleMenu[4], 1)), "2026-03-10T19:00:00Z", OrderStatus.ON_THE_WAY),
        order("QC-1044", grocery, listOf(CartLine(sampleGroceries[0], 6), CartLine(sampleGroceries[3], 1)), "2026-03-08T17:20:00Z", OrderStatus.DELIVERED),
        order(
            "QC-1042", ramen,
            listOf(CartLine(MenuItem(1030, 3, "Japanese Ramen Soup", "", "Ramen", 1599, 0.0, null, "", null, emptySet()), 2)),
            "2026-03-05T12:10:00Z", OrderStatus.DELIVERED,
        ),
    )
}
