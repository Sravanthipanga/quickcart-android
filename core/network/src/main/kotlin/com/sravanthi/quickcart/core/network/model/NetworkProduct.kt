package com.sravanthi.quickcart.core.network.model

import com.sravanthi.quickcart.core.model.Product
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class NetworkProductPage(
    val products: List<NetworkProduct>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

/** The API's product. Unknown fields are ignored, so the API can grow without breaking the app. */
@Serializable
data class NetworkProduct(
    val id: Long,
    val title: String,
    val description: String = "",
    val category: String = "",
    val brand: String? = null,
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val thumbnail: String = "",
    val images: List<String> = emptyList(),
)

/** Converts the API's dollar amount to cents once, at the boundary, so the app never does money math in doubles. */
fun NetworkProduct.asExternalModel() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand?.takeIf { it.isNotBlank() },
    priceCents = (price * 100).roundToLong(),
    discountPercent = discountPercentage,
    rating = rating,
    stock = stock,
    thumbnailUrl = thumbnail,
    imageUrls = images,
)
