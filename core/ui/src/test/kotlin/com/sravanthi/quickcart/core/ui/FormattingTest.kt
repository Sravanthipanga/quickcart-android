package com.sravanthi.quickcart.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {
    @Test
    fun `counts abbreviate above a thousand`() {
        assertEquals("312", formatCount(312))
        assertEquals("1.3k", formatCount(1284))
        assertEquals("3k", formatCount(3020))
        assertEquals("12k", formatCount(12_500))
    }

    @Test
    fun `ratings show one decimal`() {
        assertEquals("4.0", formatRating(4.0))
        assertEquals("4.7", formatRating(4.66))
    }
}
