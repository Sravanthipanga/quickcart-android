package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class StoreTest {

    private fun store(opens: String, closes: String, fee: Long = 199) = Store(
        id = 1, name = "Taco Fiesta", type = StoreType.RESTAURANT, cuisine = "Mexican", rating = 4.5, ratingCount = 10,
        deliveryMinMinutes = 15, deliveryMaxMinutes = 25, deliveryFeeCents = fee, minimumOrderCents = 1000,
        opensAt = LocalTime.parse(opens), closesAt = LocalTime.parse(closes), distanceMiles = 1.0, imageUrl = "",
    )

    private fun t(text: String) = LocalTime.parse(text)

    @Test
    fun `daytime hours include opening and exclude closing time`() {
        val store = store("11:00", "22:00")
        assertTrue(store.isOpenAt(t("11:00")))
        assertTrue(store.isOpenAt(t("21:59")))
        assertFalse(store.isOpenAt(t("22:00")))
        assertFalse(store.isOpenAt(t("08:00")))
    }

    @Test
    fun `hours past midnight wrap around`() {
        val store = store("10:00", "02:00")
        assertTrue(store.isOpenAt(t("23:30")))
        assertTrue(store.isOpenAt(t("01:30")))
        assertFalse(store.isOpenAt(t("03:00")))
        assertFalse(store.isOpenAt(t("09:59")))
    }

    @Test
    fun `equal open and close times mean 24 hours`() {
        val store = store("00:00", "00:00")
        assertTrue(store.isOpen24Hours)
        assertTrue(store.isOpenAt(t("03:00")))
    }

    @Test
    fun `free delivery is a zero fee`() {
        assertTrue(store("11:00", "22:00", fee = 0).hasFreeDelivery)
        assertFalse(store("11:00", "22:00", fee = 1).hasFreeDelivery)
    }
}
