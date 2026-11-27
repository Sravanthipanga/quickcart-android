package com.sravanthi.quickcart.core.testing

import com.sravanthi.quickcart.core.data.repository.CartRepository
import com.sravanthi.quickcart.core.model.AddToCartResult
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.CartLine
import com.sravanthi.quickcart.core.model.MenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory cart following the same one-store rule as the real one. Stores come from [stores]. */
class FakeCartRepository(
    private val stores: List<com.sravanthi.quickcart.core.model.Store> = sampleStores,
) : CartRepository {

    val cart = MutableStateFlow(Cart.Empty)

    override fun observeCart(): Flow<Cart> = cart

    override suspend fun add(item: MenuItem): AddToCartResult {
        val current = cart.value
        val store = current.store
        if (store != null && store.id != item.storeId) {
            return AddToCartResult.DifferentStore(store.id, store.name)
        }
        val quantity = current.quantityOf(item.id) + 1
        val lines = current.lines.filterNot { it.item.id == item.id } + CartLine(item, quantity)
        cart.value = Cart(store ?: stores.firstOrNull { it.id == item.storeId }, lines.sortedBy { it.item.id })
        return AddToCartResult.Added
    }

    override suspend fun startNewCartWith(item: MenuItem) {
        cart.value = Cart(stores.firstOrNull { it.id == item.storeId }, listOf(CartLine(item, 1)))
    }

    override suspend fun setQuantity(itemId: Long, quantity: Int) {
        val current = cart.value
        val lines = current.lines.mapNotNull { line ->
            when {
                line.item.id != itemId -> line
                quantity <= 0 -> null
                else -> line.copy(quantity = quantity)
            }
        }
        cart.value = if (lines.isEmpty()) Cart.Empty else current.copy(lines = lines)
    }

    override suspend fun clear() {
        cart.value = Cart.Empty
    }
}
