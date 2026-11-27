package com.sravanthi.quickcart.core.database.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderLine
import com.sravanthi.quickcart.core.model.OrderPricing
import com.sravanthi.quickcart.core.model.OrderStatus
import java.time.Instant

/** A placed order and its last known status. */
@Entity(tableName = "orders", indices = [Index("placedAtMillis")])
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val storeId: Long,
    val storeName: String,
    val storeImageUrl: String,
    val subtotalCents: Long,
    val deliveryFeeCents: Long,
    val serviceFeeCents: Long,
    val minimumOrderCents: Long,
    val placedAtMillis: Long,
    val etaMinMinutes: Int,
    val etaMaxMinutes: Int,
    val status: String,
)

/** What was ordered, as it was at the time. */
@Entity(
    tableName = "order_lines",
    primaryKeys = ["orderId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = OrderEntity::class, parentColumns = ["orderId"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class OrderLineEntity(
    val orderId: String,
    val itemId: Long,
    val name: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val unit: String?,
    val imageUrl: String,
)

data class OrderWithLines(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "orderId", entityColumn = "orderId")
    val lines: List<OrderLineEntity>,
)

fun OrderWithLines.asExternalModel() = Order(
    id = order.orderId,
    storeId = order.storeId,
    storeName = order.storeName,
    storeImageUrl = order.storeImageUrl,
    lines = lines.map { OrderLine(it.itemId, it.name, it.quantity, it.unitPriceCents, it.unit, it.imageUrl) },
    pricing = OrderPricing(order.subtotalCents, order.deliveryFeeCents, order.serviceFeeCents, order.minimumOrderCents),
    placedAt = Instant.ofEpochMilli(order.placedAtMillis),
    etaMinMinutes = order.etaMinMinutes,
    etaMaxMinutes = order.etaMaxMinutes,
    status = OrderStatus.entries.firstOrNull { it.name == order.status } ?: OrderStatus.PLACED,
)
