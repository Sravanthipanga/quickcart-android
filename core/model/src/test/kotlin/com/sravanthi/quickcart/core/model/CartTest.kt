package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class CartTest {

    private val store = Store(
        id = 1, name = "Bella Napoli", type = StoreType.RESTAURANT, cuisine = "Italian", rating = 4.7, ratingCount = 1,
        deliveryMinMinutes = 25, deliveryMaxMinutes = 35, deliveryFeeCents = 199, minimumOrderCents = 1200,
        opensAt = LocalTime.of(11, 0), closesAt = LocalTime.of(23, 0), distanceMiles = 1.0, imageUrl = "",
    )

    private fun item(id: Long, price: Long, discount: Double = 0.0) =
        MenuItem(id, 1, "Item $id", "", "Pizza", price, discount, null, "", null, emptySet())

    @Test
    fun `cart totals use sale prices and quantities`() {
        val cart = Cart(store, listOf(CartLine(item(1, 1499), 2), CartLine(item(2, 1000, discount = 10.0), 1)))
        assertEquals(3, cart.itemCount)
        assertEquals(1499L * 2 + 900, cart.subtotalCents)
        assertEquals(2, cart.quantityOf(1))
        assertEquals(0, cart.quantityOf(99))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero quantities are not cart lines`() {
        CartLine(item(1, 100), 0)
    }

    @Test
    fun `service fee is 5 percent within its floor and cap`() {
        assertEquals(0L, serviceFeeFor(0))
        assertEquals(99L, serviceFeeFor(1_000))
        assertEquals(150L, serviceFeeFor(3_000))
        assertEquals(499L, serviceFeeFor(50_000))
    }

    @Test
    fun `pricing adds delivery and service fees to the subtotal`() {
        val pricing = priceOrder(subtotalCents = 2_998, store = store)
        assertEquals(199L, pricing.deliveryFeeCents)
        assertEquals(150L, pricing.serviceFeeCents)
        assertEquals(2_998L + 199 + 150, pricing.totalCents)
        assertTrue(pricing.meetsMinimum)
    }

    @Test
    fun `below the minimum it says how much more to add`() {
        val pricing = priceOrder(subtotalCents = 850, store = store)
        assertFalse(pricing.meetsMinimum)
        assertEquals(350L, pricing.amountToMinimumCents)
    }

    @Test
    fun `an empty cart has no fees`() {
        assertEquals(0L, priceOrder(0, store).totalCents)
    }
}
