package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderConfirmation
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    /** Sends the current cart to the server. The cart is emptied, and the order saved, only when it is accepted. */
    suspend fun placeOrder(): PlaceOrderResult

    /** Order history, newest first, from the device. */
    fun observeOrders(): Flow<List<Order>>

    fun observeOrder(orderId: String): Flow<Order?>

    /** Asks the server for the latest status. Returns false when offline; the last known status stays. */
    suspend fun refreshStatus(orderId: String): Boolean

    /** Replaces the cart with the items of a past order. */
    suspend fun reorder(orderId: String)
}

sealed interface PlaceOrderResult {
    data class Placed(val confirmation: OrderConfirmation) : PlaceOrderResult

    /** The server refused the order, e.g. the store closed or an item sold out. [message] is shown to the user. */
    data class Rejected(val message: String) : PlaceOrderResult

    /** Couldn't reach the server; the cart is kept so the user can try again. */
    data object Offline : PlaceOrderResult
}
