package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.model.OrderConfirmation

interface OrderRepository {
    /** Sends the current cart to the server. The cart is emptied only when the order is accepted. */
    suspend fun placeOrder(): PlaceOrderResult
}

sealed interface PlaceOrderResult {
    data class Placed(val confirmation: OrderConfirmation) : PlaceOrderResult

    /** The server refused the order, e.g. the store closed or an item sold out. [message] is shown to the user. */
    data class Rejected(val message: String) : PlaceOrderResult

    /** Couldn't reach the server; the cart is kept so the user can try again. */
    data object Offline : PlaceOrderResult
}
