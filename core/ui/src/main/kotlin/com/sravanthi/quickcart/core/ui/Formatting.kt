package com.sravanthi.quickcart.core.ui

import java.util.Locale

/** 312 → "312", 1284 → "1.2k", 12500 → "12k". */
fun formatCount(count: Int): String = when {
    count < 1_000 -> count.toString()
    count < 10_000 -> String.format(Locale.US, "%.1fk", count / 1000.0).replace(".0k", "k")
    else -> "${count / 1000}k"
}

fun formatRating(rating: Double): String = String.format(Locale.US, "%.1f", rating)
