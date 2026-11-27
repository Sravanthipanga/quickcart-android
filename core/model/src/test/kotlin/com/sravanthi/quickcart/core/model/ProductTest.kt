package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTest {

    private fun product(priceCents: Long = 1_000, discount: Double = 0.0, stock: Int = 50) = Product(
        id = 1, title = "Phone", description = "", category = "smartphones", brand = null,
        priceCents = priceCents, discountPercent = discount, rating = 4.5, stock = stock, thumbnailUrl = "",
    )

    @Test
    fun `sale price applies the discount and rounds to the nearest cent`() {
        assertEquals(850L, product(1_000, 15.0).salePriceCents)
        assertEquals(1_250L, product(1_499, 16.61).salePriceCents) // 1250.0161 -> 1250
        assertEquals(1_000L, product(1_000, 0.0).salePriceCents)
    }

    @Test
    fun `out-of-range discounts are clamped`() {
        assertEquals(0L, product(1_000, 150.0).salePriceCents)
        assertEquals(1_000L, product(1_000, -5.0).salePriceCents)
    }

    @Test
    fun `tiny discounts do not get a badge`() {
        assertFalse(product(discount = 0.5).hasDiscount)
        assertTrue(product(discount = 1.0).hasDiscount)
    }

    @Test
    fun `stock level thresholds`() {
        assertEquals(StockLevel.OutOfStock, product(stock = 0).stockLevel)
        assertEquals(StockLevel.Low, product(stock = 9).stockLevel)
        assertEquals(StockLevel.InStock, product(stock = 10).stockLevel)
    }

    @Test
    fun `prices format as US dollars`() {
        assertEquals("$0.05", formatPrice(5))
        assertEquals("$9.99", formatPrice(999))
        assertEquals("$1,299.00", formatPrice(129_900))
    }
}
