package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.data.repository.PlaceOrderResult
import com.sravanthi.quickcart.core.model.OrderConfirmation
import com.sravanthi.quickcart.core.model.priceOrder

/** Accepts orders from [cart] unless [nextResult] says otherwise. */
class FakeOrderRepository(private val cart: FakeCartRepository) : OrderRepository {
    var nextResult: PlaceOrderResult? = null
    var placedCount = 0
        private set

    override suspend fun placeOrder(): PlaceOrderResult {
        nextResult?.let { return it }
        val current = cart.cart.value
        val store = checkNotNull(current.store)
        placedCount++
        cart.clear()
        return PlaceOrderResult.Placed(
            OrderConfirmation("QC-${1041 + placedCount}", store.name, priceOrder(current.subtotalCents, store), 25, 35),
        )
    }
}
