package com.sravanthi.quickcart.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sravanthi.quickcart.core.database.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAtMillis")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items ORDER BY addedAtMillis")
    suspend fun getAll(): List<CartItemEntity>

    @Query("SELECT * FROM cart_items WHERE itemId = :itemId")
    suspend fun get(itemId: Long): CartItemEntity?

    /** The store the cart belongs to, or null when the cart is empty. */
    @Query("SELECT storeId FROM cart_items LIMIT 1")
    suspend fun cartStoreId(): Long?

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE itemId = :itemId")
    suspend fun delete(itemId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()

    /** Empties the cart and starts it again with [item], in one step. */
    @Transaction
    suspend fun replaceWith(item: CartItemEntity) {
        clear()
        upsert(item)
    }
}
