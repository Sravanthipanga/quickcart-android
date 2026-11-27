package com.sravanthi.quickcart.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sravanthi.quickcart.core.model.CartLine
import com.sravanthi.quickcart.core.model.MenuItem

/**
 * A cart line with a snapshot of the item as it was when added, so the cart can be shown even if
 * the store's menu is not cached. Checkout re-prices everything on the server.
 */
@Entity(tableName = "cart_items", indices = [Index("storeId")])
data class CartItemEntity(
    @PrimaryKey val itemId: Long,
    val storeId: Long,
    val name: String,
    val description: String,
    val section: String,
    val priceCents: Long,
    val discountPercent: Double,
    val unit: String?,
    val imageUrl: String,
    val quantity: Int,
    val addedAtMillis: Long,
)

fun CartItemEntity.asCartLine() = CartLine(
    item = MenuItem(
        id = itemId,
        storeId = storeId,
        name = name,
        description = description,
        section = section,
        priceCents = priceCents,
        discountPercent = discountPercent,
        unit = unit,
        imageUrl = imageUrl,
        calories = null,
        tags = emptySet(),
    ),
    quantity = quantity,
)

fun MenuItem.asCartEntity(quantity: Int, addedAtMillis: Long) = CartItemEntity(
    itemId = id,
    storeId = storeId,
    name = name,
    description = description,
    section = section,
    priceCents = priceCents,
    discountPercent = discountPercent,
    unit = unit,
    imageUrl = imageUrl,
    quantity = quantity,
    addedAtMillis = addedAtMillis,
)
