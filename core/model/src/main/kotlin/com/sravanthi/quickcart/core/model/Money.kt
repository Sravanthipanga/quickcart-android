package com.sravanthi.quickcart.core.model

import java.util.Locale
import kotlin.math.roundToLong

/** Formats cents as dollars, e.g. 129999 → "$1,299.99". All money in QuickCart is whole cents. */
fun formatPrice(cents: Long): String {
    val dollars = cents / 100
    val remainder = (cents % 100).toString().padStart(2, '0')
    return "$" + String.format(Locale.US, "%,d", dollars) + "." + remainder
}

/** [cents] reduced by [percent], rounded to the nearest cent. Out-of-range discounts are clamped. */
fun applyDiscount(cents: Long, percent: Double): Long =
    (cents * (1 - percent.coerceIn(0.0, 100.0) / 100)).roundToLong()
