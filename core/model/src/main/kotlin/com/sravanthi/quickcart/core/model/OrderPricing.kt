package com.sravanthi.quickcart.core.model

import kotlin.math.roundToLong

/**
 * The price breakdown of an order. Both the app's checkout preview and the order server use
 * [priceOrder], so what the customer sees before ordering is what the server charges.
 */
data class OrderPricing(
    val subtotalCents: Long,
    val deliveryFeeCents: Long,
    val serviceFeeCents: Long,
    val minimumOrderCents: Long,
) {
    val totalCents: Long get() = subtotalCents + deliveryFeeCents + serviceFeeCents

    /** How much more to add before the store accepts the order; 0 when the minimum is met. */
    val amountToMinimumCents: Long get() = (minimumOrderCents - subtotalCents).coerceAtLeast(0)

    val meetsMinimum: Boolean get() = amountToMinimumCents == 0L
}

/** Service fee: 5% of the subtotal, at least $0.99 and at most $4.99. No fee on an empty cart. */
fun serviceFeeFor(subtotalCents: Long): Long =
    if (subtotalCents <= 0) 0 else (subtotalCents * SERVICE_FEE_RATE).roundToLong().coerceIn(MIN_SERVICE_FEE, MAX_SERVICE_FEE)

fun priceOrder(subtotalCents: Long, store: Store): OrderPricing = OrderPricing(
    subtotalCents = subtotalCents,
    deliveryFeeCents = if (subtotalCents > 0) store.deliveryFeeCents else 0,
    serviceFeeCents = serviceFeeFor(subtotalCents),
    minimumOrderCents = store.minimumOrderCents,
)

private const val SERVICE_FEE_RATE = 0.05
private const val MIN_SERVICE_FEE = 99L
private const val MAX_SERVICE_FEE = 499L

/** A placed order, as confirmed by the server. */
data class OrderConfirmation(
    val orderId: String,
    val storeName: String,
    val pricing: OrderPricing,
    val etaMinMinutes: Int,
    val etaMaxMinutes: Int,
    val placedAt: java.time.Instant,
)
