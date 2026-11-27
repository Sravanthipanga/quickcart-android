package com.sravanthi.quickcart.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sravanthi.quickcart.core.model.Product

/**
 * A cached catalog product. [position] keeps the API's order, so the offline list
 * looks exactly like the online one.
 */
@Entity(tableName = "products", indices = [Index("position")])
data class ProductEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val priceCents: Long,
    val discountPercent: Double,
    val rating: Double,
    val stock: Int,
    val thumbnailUrl: String,
    val imageUrls: List<String>,
    val position: Int,
)

fun ProductEntity.asExternalModel() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    priceCents = priceCents,
    discountPercent = discountPercent,
    rating = rating,
    stock = stock,
    thumbnailUrl = thumbnailUrl,
    imageUrls = imageUrls,
)

fun Product.asEntity(position: Int) = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    priceCents = priceCents,
    discountPercent = discountPercent,
    rating = rating,
    stock = stock,
    thumbnailUrl = thumbnailUrl,
    imageUrls = imageUrls,
    position = position,
)
