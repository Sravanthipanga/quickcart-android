package com.sravanthi.quickcart.core.model

import kotlin.math.roundToLong

/**
 * A product in the catalog. Prices are whole cents, so money is never a floating-point value.
 */
data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val priceCents: Long,
    val discountPercent: Double,
    val rating: Double,
    val stock: Int,
    val thumbnailUrl: String,
    val imageUrls: List<String> = emptyList(),
) {
    /** The price after discount, rounded to the nearest cent. */
    val salePriceCents: Long
        get() = (priceCents * (1 - discountPercent.coerceIn(0.0, 100.0) / 100)).roundToLong()

    /** Show a discount badge only for meaningful discounts. */
    val hasDiscount: Boolean get() = discountPercent >= 1.0

    val stockLevel: StockLevel
        get() = when {
            stock <= 0 -> StockLevel.OutOfStock
            stock < LOW_STOCK_THRESHOLD -> StockLevel.Low
            else -> StockLevel.InStock
        }

    companion object {
        const val LOW_STOCK_THRESHOLD = 10
    }
}

enum class StockLevel { InStock, Low, OutOfStock }

/** Formats cents as dollars, e.g. 129999 → "$1,299.99". */
fun formatPrice(cents: Long): String {
    val dollars = cents / 100
    val remainder = (cents % 100).toString().padStart(2, '0')
    return "$" + String.format(java.util.Locale.US, "%,d", dollars) + "." + remainder
}
