package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.model.Product

/** Screenshot tests run with the module as working directory, so this is the repo's docs/screenshots. */
const val SCREENSHOT_DIR = "../../docs/screenshots"

/** A Pixel-sized phone in US English, for consistent screenshots. */
const val PHONE_QUALIFIERS = "en-rUS-w411dp-h891dp-xxhdpi"

/** Realistic sample products for previews, tests and screenshots. */
val sampleProducts: List<Product> = listOf(
    Product(1, "iPhone 15 Pro", "Titanium design with the A17 Pro chip.", "smartphones", "Apple", 99_900, 8.0, 4.8, 42, ""),
    Product(2, "Wireless Noise-Cancelling Headphones", "Up to 30 hours of battery.", "audio", "Sonic", 24_999, 15.0, 4.6, 7, ""),
    Product(3, "Organic Cold Brew Coffee", "Smooth, low-acid, 32 oz.", "groceries", "Bean & Co", 1_299, 0.0, 4.4, 120, ""),
    Product(4, "Running Shoes Pro", "Lightweight cushioning for daily runs.", "mens-shoes", "Stride", 12_950, 20.0, 4.7, 0, ""),
    Product(5, "Ceramic Plant Pot Set", "Three pots with drainage trays.", "home-decoration", null, 3_450, 5.0, 4.2, 18, ""),
    Product(6, "Smart Fitness Watch", "Heart rate, GPS and sleep tracking.", "mens-watches", "Pulse", 17_999, 12.5, 4.5, 3, ""),
)
