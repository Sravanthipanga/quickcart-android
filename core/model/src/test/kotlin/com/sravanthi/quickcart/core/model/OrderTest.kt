package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class OrderTest {

    private val placed = Instant.parse("2026-03-10T19:00:00Z")
    private fun after(minutes: Long, seconds: Long = 0) = placed.plusSeconds(minutes * 60 + seconds)

    @Test
    fun `status moves through each stage of a 40-minute window`() {
        assertEquals(OrderStatus.PLACED, orderStatusAt(placed, 40, after(0)))
        assertEquals(OrderStatus.PLACED, orderStatusAt(placed, 40, after(3, 59)))
        assertEquals(OrderStatus.PREPARING, orderStatusAt(placed, 40, after(4)))
        assertEquals(OrderStatus.PICKED_UP, orderStatusAt(placed, 40, after(18)))
        assertEquals(OrderStatus.ON_THE_WAY, orderStatusAt(placed, 40, after(24)))
        assertEquals(OrderStatus.ON_THE_WAY, orderStatusAt(placed, 40, after(39, 59)))
        assertEquals(OrderStatus.DELIVERED, orderStatusAt(placed, 40, after(40)))
    }

    @Test
    fun `a clock slightly behind the order time still reads as placed`() {
        assertEquals(OrderStatus.PLACED, orderStatusAt(placed, 40, placed.minusSeconds(30)))
    }

    @Test
    fun `orders know their arrival time, size and whether they are active`() {
        val order = Order(
            id = "QC-1042", storeId = 1, storeName = "Bella Napoli", storeImageUrl = "",
            lines = listOf(OrderLine(1001, "Pizza", 2, 1499, null, ""), OrderLine(1006, "Bruschetta", 1, 899, null, "")),
            pricing = OrderPricing(3897, 199, 195, 1200), placedAt = placed, etaMinMinutes = 25, etaMaxMinutes = 35,
            status = OrderStatus.ON_THE_WAY,
        )
        assertEquals(placed.plus(Duration.ofMinutes(35)), order.arriveBy)
        assertEquals(3, order.itemCount)
        assertEquals(2998L, order.lines.first().totalCents)
        assertTrue(order.isActive)
        assertFalse(order.copy(status = OrderStatus.DELIVERED).isActive)
    }
}
