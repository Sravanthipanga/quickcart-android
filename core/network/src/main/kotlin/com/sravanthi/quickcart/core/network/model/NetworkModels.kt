package com.sravanthi.quickcart.core.network.model

import com.sravanthi.quickcart.core.model.ItemTag
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class NetworkStorePage(val stores: List<NetworkStore>, val total: Int, val skip: Int, val limit: Int)

@Serializable
data class NetworkMenu(val items: List<NetworkMenuItem>)

@Serializable
data class NetworkSearchResults(val stores: List<NetworkStore>, val items: List<NetworkSearchHit>)

/** A matching item with its store's name, so results from many stores can be shown together. */
@Serializable
data class NetworkSearchHit(val item: NetworkMenuItem, val storeName: String)

@Serializable
data class NetworkStore(
    val id: Long,
    val name: String,
    val type: String,
    val cuisine: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val deliveryMinMinutes: Int,
    val deliveryMaxMinutes: Int,
    val deliveryFeeCents: Long,
    val minimumOrderCents: Long = 0,
    val opensAt: String,
    val closesAt: String,
    val distanceMiles: Double = 0.0,
    val imageUrl: String = "",
)

@Serializable
data class NetworkMenuItem(
    val id: Long,
    val storeId: Long,
    val name: String,
    val description: String = "",
    val section: String,
    val priceCents: Long,
    val discountPercent: Double = 0.0,
    val unit: String? = null,
    val imageUrl: String = "",
    val calories: Int? = null,
    val tags: List<String> = emptyList(),
)

/** Unknown store types fall back to RESTAURANT so a new type on the server never crashes the app. */
fun NetworkStore.asExternalModel() = Store(
    id = id,
    name = name,
    type = StoreType.entries.firstOrNull { it.name == type } ?: StoreType.RESTAURANT,
    cuisine = cuisine,
    rating = rating,
    ratingCount = ratingCount,
    deliveryMinMinutes = deliveryMinMinutes,
    deliveryMaxMinutes = deliveryMaxMinutes,
    deliveryFeeCents = deliveryFeeCents,
    minimumOrderCents = minimumOrderCents,
    opensAt = LocalTime.parse(opensAt),
    closesAt = LocalTime.parse(closesAt),
    distanceMiles = distanceMiles,
    imageUrl = imageUrl,
)

/** Unknown tags are ignored. */
fun NetworkMenuItem.asExternalModel() = MenuItem(
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
    tags = tags.mapNotNull { tag -> ItemTag.entries.firstOrNull { it.name.equals(tag, ignoreCase = true) } }.toSet(),
)

@Serializable
data class NetworkOrderRequest(val storeId: Long, val lines: List<NetworkOrderLine>)

@Serializable
data class NetworkOrderLine(val itemId: Long, val quantity: Int)

@Serializable
data class NetworkOrderConfirmation(
    val orderId: String,
    val storeName: String,
    val subtotalCents: Long,
    val deliveryFeeCents: Long,
    val serviceFeeCents: Long,
    val minimumOrderCents: Long,
    val etaMinMinutes: Int,
    val etaMaxMinutes: Int,
)

/** The error body the API returns with 4xx responses. */
@Serializable
data class NetworkError(val message: String)

fun NetworkOrderConfirmation.asExternalModel() = com.sravanthi.quickcart.core.model.OrderConfirmation(
    orderId = orderId,
    storeName = storeName,
    pricing = com.sravanthi.quickcart.core.model.OrderPricing(subtotalCents, deliveryFeeCents, serviceFeeCents, minimumOrderCents),
    etaMinMinutes = etaMinMinutes,
    etaMaxMinutes = etaMaxMinutes,
)
