package com.sravanthi.quickcart.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sravanthi.quickcart.core.model.ItemTag
import com.sravanthi.quickcart.core.model.MenuItem

/** A cached menu item. [position] keeps the store's menu order. */
@Entity(tableName = "menu_items", indices = [Index("storeId")])
data class MenuItemEntity(
    @PrimaryKey val id: Long,
    val storeId: Long,
    val name: String,
    val description: String,
    val section: String,
    val priceCents: Long,
    val discountPercent: Double,
    val unit: String?,
    val imageUrl: String,
    val calories: Int?,
    /** Comma-separated [ItemTag] names. */
    val tags: String,
    val position: Int,
)

fun MenuItemEntity.asExternalModel() = MenuItem(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    section = section,
    priceCents = priceCents,
    discountPercent = discountPercent,
    unit = unit,
    imageUrl = imageUrl,
    calories = calories,
    tags = tags.split(',').mapNotNull { name -> ItemTag.entries.firstOrNull { it.name == name } }.toSet(),
)

fun MenuItem.asEntity(position: Int) = MenuItemEntity(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    section = section,
    priceCents = priceCents,
    discountPercent = discountPercent,
    unit = unit,
    imageUrl = imageUrl,
    calories = calories,
    tags = tags.joinToString(",") { it.name },
    position = position,
)
