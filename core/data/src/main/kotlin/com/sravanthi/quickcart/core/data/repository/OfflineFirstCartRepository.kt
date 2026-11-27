package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.asCartEntity
import com.sravanthi.quickcart.core.database.model.asCartLine
import com.sravanthi.quickcart.core.database.model.asExternalModel
import com.sravanthi.quickcart.core.model.AddToCartResult
import com.sravanthi.quickcart.core.model.Cart
import com.sravanthi.quickcart.core.model.MenuItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class OfflineFirstCartRepository @Inject constructor(
    private val database: QuickCartDatabase,
    private val clock: Clock,
) : CartRepository {

    private val cartDao = database.cartDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCart(): Flow<Cart> = cartDao.observeAll().flatMapLatest { entities ->
        if (entities.isEmpty()) {
            flowOf(Cart.Empty)
        } else {
            val lines = entities.map { it.asCartLine() }
            database.storeDao().observeById(entities.first().storeId).map { store -> Cart(store?.asExternalModel(), lines) }
        }
    }

    override suspend fun add(item: MenuItem): AddToCartResult {
        val cartStoreId = cartDao.cartStoreId()
        if (cartStoreId != null && cartStoreId != item.storeId) {
            val name = database.storeDao().namesFor(listOf(cartStoreId)).firstOrNull()?.name.orEmpty()
            return AddToCartResult.DifferentStore(cartStoreId, name)
        }
        val existing = cartDao.get(item.id)
        if (existing != null) {
            cartDao.updateQuantity(item.id, existing.quantity + 1)
        } else {
            cartDao.upsert(item.asCartEntity(quantity = 1, addedAtMillis = clock.millis()))
        }
        return AddToCartResult.Added
    }

    override suspend fun startNewCartWith(item: MenuItem) =
        cartDao.replaceWith(item.asCartEntity(quantity = 1, addedAtMillis = clock.millis()))

    override suspend fun setQuantity(itemId: Long, quantity: Int) {
        if (quantity <= 0) cartDao.delete(itemId) else cartDao.updateQuantity(itemId, quantity)
    }

    override suspend fun clear() = cartDao.clear()
}
