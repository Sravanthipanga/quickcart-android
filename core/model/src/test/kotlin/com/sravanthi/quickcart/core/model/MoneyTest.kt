package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    private fun item(price: Long, discount: Double, section: String = "Produce", id: Long = 1) = MenuItem(
        id = id, storeId = 8, name = "Apple", description = "", section = section, priceCents = price,
        discountPercent = discount, unit = "each", imageUrl = "", calories = null, tags = emptySet(),
    )

    @Test
    fun `prices format as US dollars`() {
        assertEquals("$0.05", formatPrice(5))
        assertEquals("$9.99", formatPrice(999))
        assertEquals("$1,299.00", formatPrice(129_900))
    }

    @Test
    fun `discounts round to the nearest cent and are clamped`() {
        assertEquals(174L, applyDiscount(199, 12.62))
        assertEquals(0L, applyDiscount(1_000, 150.0))
        assertEquals(1_000L, applyDiscount(1_000, -5.0))
    }

    @Test
    fun `items expose sale price and only meaningful discounts`() {
        assertEquals(174L, item(199, 12.62).salePriceCents)
        assertTrue(item(199, 1.0).hasDiscount)
        assertFalse(item(199, 0.16).hasDiscount)
    }

    @Test
    fun `sections keep first-seen order`() {
        val items = listOf(item(1, 0.0, "Pizza", 1), item(1, 0.0, "Pasta", 2), item(1, 0.0, "Pizza", 3))
        assertEquals(listOf("Pizza", "Pasta"), items.bySection().keys.toList())
        assertEquals(listOf(1L, 3L), items.bySection().getValue("Pizza").map { it.id })
    }
}
