package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.model.AddToCartResult
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.MenuItem
import kotlinx.coroutines.flow.Flow

/** The cart, saved on the device so it survives closing the app. Holds items from one store at a time. */
interface CartRepository {
    fun observeCart(): Flow<Cart>

    /** Adds one of [item], or reports that the cart belongs to another store. */
    suspend fun add(item: MenuItem): AddToCartResult

    /** Empties the cart and starts a new one with [item] (after the user agreed to switch stores). */
    suspend fun startNewCartWith(item: MenuItem)

    /** Sets a line's quantity; 0 or less removes it. */
    suspend fun setQuantity(itemId: Long, quantity: Int)

    suspend fun clear()
}
