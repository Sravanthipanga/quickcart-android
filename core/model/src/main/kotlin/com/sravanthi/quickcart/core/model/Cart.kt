package com.sravanthi.quickcart.core.model

/** One item in the cart. The price is the item's price when it was added; checkout uses the server's price. */
data class CartLine(val item: MenuItem, val quantity: Int) {
    init {
        require(quantity > 0) { "Cart quantities are positive; remove the line instead" }
    }

    val totalCents: Long get() = item.salePriceCents * quantity
}

/** The cart: items from a single [store], like most delivery apps. Empty when [lines] is empty. */
data class Cart(val store: Store?, val lines: List<CartLine>) {
    val isEmpty: Boolean get() = lines.isEmpty()
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val subtotalCents: Long get() = lines.sumOf { it.totalCents }

    fun quantityOf(itemId: Long): Int = lines.firstOrNull { it.item.id == itemId }?.quantity ?: 0

    companion object {
        val Empty = Cart(store = null, lines = emptyList())
    }
}

/** What happened when adding an item. */
sealed interface AddToCartResult {
    data object Added : AddToCartResult

    /** The cart holds items from another store; the user must choose to start a new cart. */
    data class DifferentStore(val currentStoreId: Long, val currentStoreName: String) : AddToCartResult
}
