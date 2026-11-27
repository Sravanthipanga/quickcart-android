package com.sravanthi.quickcart.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinksTest {

    @Test
    fun `links fill in their patterns`() {
        assertEquals("quickcart://orders/QC-1042", DeepLinks.order("QC-1042"))
        assertEquals(DeepLinks.ORDER_PATTERN.replace("{orderId}", "QC-1042"), DeepLinks.order("QC-1042"))
        assertEquals(DeepLinks.STORE_PATTERN.replace("{storeId}", "8"), DeepLinks.store(8))
    }
}
