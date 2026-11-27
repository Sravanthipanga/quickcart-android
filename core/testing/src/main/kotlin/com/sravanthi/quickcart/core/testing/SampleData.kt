package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.model.ItemTag
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import java.time.LocalTime

/** Realistic sample marketplace data for tests, previews and screenshots. */
private fun store(
    id: Long, name: String, type: StoreType, cuisine: String, rating: Double, ratingCount: Int,
    eta: IntRange, feeCents: Long, opens: String, closes: String,
) = Store(
    id = id, name = name, type = type, cuisine = cuisine, rating = rating, ratingCount = ratingCount,
    deliveryMinMinutes = eta.first, deliveryMaxMinutes = eta.last, deliveryFeeCents = feeCents, minimumOrderCents = 1000,
    opensAt = LocalTime.parse(opens), closesAt = LocalTime.parse(closes), distanceMiles = 1.2, imageUrl = "",
)

val sampleStores: List<Store> = listOf(
    store(1, "Bella Napoli Pizzeria", StoreType.RESTAURANT, "Italian · Pizza", 4.7, 1284, 25..35, 199, "11:00", "23:00"),
    store(3, "Sakura Ramen House", StoreType.RESTAURANT, "Japanese · Ramen", 4.9, 2103, 20..30, 0, "11:30", "22:00"),
    store(8, "FreshMart Grocery", StoreType.GROCERY, "Groceries · Produce · Pantry", 4.7, 3420, 35..60, 399, "07:00", "22:00"),
    store(7, "Island Grill", StoreType.RESTAURANT, "Hawaiian · Grill", 4.4, 312, 30..45, 299, "16:00", "23:00"),
    store(9, "Corner Market 24/7", StoreType.CONVENIENCE, "Convenience · Essentials", 4.3, 524, 10..20, 299, "00:00", "00:00"),
)

private fun item(
    id: Long, storeId: Long, name: String, description: String, section: String, price: Long,
    discount: Double = 0.0, unit: String? = null, tags: Set<ItemTag> = emptySet(),
) = MenuItem(id, storeId, name, description, section, price, discount, unit, "", null, tags)

val sampleMenu: List<MenuItem> = listOf(
    item(1001, 1, "Classic Margherita Pizza", "Wood-fired, San Marzano tomato, fresh mozzarella and basil.", "Pizza", 1499,
        tags = setOf(ItemTag.POPULAR, ItemTag.VEGETARIAN)),
    item(1002, 1, "Italian Margherita Pizza", "Thin crust, buffalo mozzarella, basil and olive oil.", "Pizza", 1599,
        tags = setOf(ItemTag.VEGETARIAN)),
    item(1003, 1, "Chicken Alfredo Pasta", "Fettuccine in a creamy parmesan sauce with grilled chicken.", "Pasta", 1699),
    item(1004, 1, "Shrimp Scampi Pasta", "Garlic butter shrimp with linguine, lemon and parsley.", "Pasta", 1899),
    item(1006, 1, "Tomato Basil Bruschetta", "Toasted ciabatta with tomato, basil and garlic.", "Starters", 899,
        tags = setOf(ItemTag.VEGETARIAN)),
    item(1009, 1, "Italian Tiramisu", "Espresso-soaked ladyfingers with mascarpone cream.", "Desserts", 799,
        tags = setOf(ItemTag.VEGETARIAN)),
)

val sampleGroceries: List<MenuItem> = listOf(
    item(1060, 8, "Apple", "Fresh and crisp apples.", "Produce", 199, discount = 12.6, unit = "each"),
    item(1066, 8, "Strawberry", "Sweet and succulent strawberries.", "Produce", 399, unit = "1 lb"),
    item(1072, 8, "Eggs", "Fresh, farm-raised eggs.", "Dairy & eggs", 299, discount = 11.1, unit = "dozen"),
    item(1073, 8, "Milk", "Fresh and nutritious milk.", "Dairy & eggs", 349, discount = 13.7, unit = "1 gal"),
)
