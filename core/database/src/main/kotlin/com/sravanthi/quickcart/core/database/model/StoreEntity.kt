package com.sravanthi.quickcart.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import java.time.LocalTime

/** A cached store. [position] keeps the marketplace's order, so the offline list matches the online one. */
@Entity(tableName = "stores", indices = [Index("position"), Index("type")])
data class StoreEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val cuisine: String,
    val rating: Double,
    val ratingCount: Int,
    val deliveryMinMinutes: Int,
    val deliveryMaxMinutes: Int,
    val deliveryFeeCents: Long,
    val minimumOrderCents: Long,
    val opensAt: String,
    val closesAt: String,
    val distanceMiles: Double,
    val imageUrl: String,
    val position: Int,
)

fun StoreEntity.asExternalModel() = Store(
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

fun Store.asEntity(position: Int) = StoreEntity(
    id = id,
    name = name,
    type = type.name,
    cuisine = cuisine,
    rating = rating,
    ratingCount = ratingCount,
    deliveryMinMinutes = deliveryMinMinutes,
    deliveryMaxMinutes = deliveryMaxMinutes,
    deliveryFeeCents = deliveryFeeCents,
    minimumOrderCents = minimumOrderCents,
    opensAt = opensAt.toString(),
    closesAt = closesAt.toString(),
    distanceMiles = distanceMiles,
    imageUrl = imageUrl,
    position = position,
)
