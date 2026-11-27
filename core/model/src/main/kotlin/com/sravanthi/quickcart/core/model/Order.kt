package com.sravanthi.quickcart.core.model

import java.time.Duration
import java.time.Instant

/** The stages of a delivery, in order. */
enum class OrderStatus { PLACED, PREPARING, PICKED_UP, ON_THE_WAY, DELIVERED }

/** A line of a placed order, as it was when ordered. */
data class OrderLine(
    val itemId: Long,
    val name: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val unit: String?,
    val imageUrl: String,
) {
    val totalCents: Long get() = unitPriceCents * quantity
}

data class Order(
    val id: String,
    val storeId: Long,
    val storeName: String,
    val storeImageUrl: String,
    val lines: List<OrderLine>,
    val pricing: OrderPricing,
    val placedAt: Instant,
    val etaMinMinutes: Int,
    val etaMaxMinutes: Int,
    val status: OrderStatus,
) {
    val isActive: Boolean get() = status != OrderStatus.DELIVERED
    val itemCount: Int get() = lines.sumOf { it.quantity }

    /** The latest promised arrival time. */
    val arriveBy: Instant get() = placedAt.plus(Duration.ofMinutes(etaMaxMinutes.toLong()))
}

/**
 * Where an order is, based on how much of its delivery window has passed. The order server and the
 * app use this same rule, so the status the app shows from its cache matches what the server reports.
 *
 * 0–10%: placed · 10–45%: preparing · 45–60%: picked up · 60–100%: on the way · after: delivered.
 */
fun orderStatusAt(placedAt: Instant, etaMaxMinutes: Int, now: Instant): OrderStatus {
    val window = Duration.ofMinutes(etaMaxMinutes.toLong()).toMillis().coerceAtLeast(1)
    val fraction = Duration.between(placedAt, now).toMillis().toDouble() / window
    return when {
        fraction < 0.10 -> OrderStatus.PLACED
        fraction < 0.45 -> OrderStatus.PREPARING
        fraction < 0.60 -> OrderStatus.PICKED_UP
        fraction < 1.00 -> OrderStatus.ON_THE_WAY
        else -> OrderStatus.DELIVERED
    }
}
