package com.sravanthi.quickcart.core.model

import java.time.LocalTime

enum class StoreType { RESTAURANT, GROCERY, CONVENIENCE, DESSERT, DRINKS }

/** A restaurant or shop on the marketplace. */
data class Store(
    val id: Long,
    val name: String,
    val type: StoreType,
    /** e.g. "Italian · Pizza" or "Groceries · Produce". */
    val cuisine: String,
    val rating: Double,
    val ratingCount: Int,
    val deliveryMinMinutes: Int,
    val deliveryMaxMinutes: Int,
    val deliveryFeeCents: Long,
    val minimumOrderCents: Long,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val distanceMiles: Double,
    val imageUrl: String,
) {
    val hasFreeDelivery: Boolean get() = deliveryFeeCents == 0L

    /** Equal open and close times mean open around the clock. */
    val isOpen24Hours: Boolean get() = opensAt == closesAt

    /**
     * Whether the store is open at [time]. Handles stores that close after midnight,
     * e.g. 10:00–02:00 is open at 01:30 and closed at 03:00.
     */
    fun isOpenAt(time: LocalTime): Boolean = when {
        isOpen24Hours -> true
        opensAt < closesAt -> !time.isBefore(opensAt) && time.isBefore(closesAt)
        else -> !time.isBefore(opensAt) || time.isBefore(closesAt)
    }
}

/** A dish on a menu or a product on a grocery aisle. */
data class MenuItem(
    val id: Long,
    val storeId: Long,
    val name: String,
    val description: String,
    /** Menu section or grocery aisle, e.g. "Pizza" or "Produce". */
    val section: String,
    val priceCents: Long,
    val discountPercent: Double,
    /** For groceries, e.g. "per lb" or "dozen"; null for restaurant dishes. */
    val unit: String?,
    val imageUrl: String,
    val calories: Int?,
    val tags: Set<ItemTag>,
) {
    val salePriceCents: Long get() = applyDiscount(priceCents, discountPercent)

    /** Show a discount only when it is meaningful. */
    val hasDiscount: Boolean get() = discountPercent >= 1.0
}

enum class ItemTag { POPULAR, VEGETARIAN, SPICY }

/** Menu items grouped by section, keeping the order sections first appear in. */
fun List<MenuItem>.bySection(): Map<String, List<MenuItem>> = groupBy { it.section }

data class SearchResults(
    val stores: List<Store>,
    val items: List<SearchItem>,
) {
    val isEmpty: Boolean get() = stores.isEmpty() && items.isEmpty()

    companion object {
        val Empty = SearchResults(emptyList(), emptyList())
    }
}

/** An item in search results, with its store's name so results across stores make sense. */
data class SearchItem(val item: MenuItem, val storeName: String)
