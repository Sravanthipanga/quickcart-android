package com.sravanthi.quickcart.core.data.repository

import androidx.room.withTransaction
import com.sravanthi.quickcart.core.data.isRecoverableNetworkError
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.database.model.CartItemEntity
import com.sravanthi.quickcart.core.database.model.OrderEntity
import com.sravanthi.quickcart.core.database.model.OrderLineEntity
import com.sravanthi.quickcart.core.database.model.asExternalModel
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.model.NetworkError
import com.sravanthi.quickcart.core.network.model.NetworkOrderLine
import com.sravanthi.quickcart.core.network.model.NetworkOrderRequest
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import javax.inject.Inject

internal class NetworkOrderRepository @Inject constructor(
    private val api: MarketplaceApi,
    private val database: QuickCartDatabase,
    private val json: Json,
    private val clock: Clock,
) : OrderRepository {

    private val orderDao = database.orderDao()

    override suspend fun placeOrder(): PlaceOrderResult {
        val lines = database.cartDao().getAll()
        if (lines.isEmpty()) return PlaceOrderResult.Rejected(EMPTY_CART)
        val storeId = lines.first().storeId

        return try {
            val confirmation = api.placeOrder(NetworkOrderRequest(storeId, lines.map { NetworkOrderLine(it.itemId, it.quantity) }))
                .asExternalModel()
            val storeImage = database.storeDao().imageUrlOf(storeId)
            // Save the order and empty the cart together, so an accepted order is never lost or double-counted.
            database.withTransaction {
                orderDao.insert(
                    OrderEntity(
                        orderId = confirmation.orderId,
                        storeId = storeId,
                        storeName = confirmation.storeName,
                        storeImageUrl = storeImage.orEmpty(),
                        subtotalCents = confirmation.pricing.subtotalCents,
                        deliveryFeeCents = confirmation.pricing.deliveryFeeCents,
                        serviceFeeCents = confirmation.pricing.serviceFeeCents,
                        minimumOrderCents = confirmation.pricing.minimumOrderCents,
                        placedAtMillis = confirmation.placedAt.toEpochMilli(),
                        etaMinMinutes = confirmation.etaMinMinutes,
                        etaMaxMinutes = confirmation.etaMaxMinutes,
                        status = OrderStatus.PLACED.name,
                    ),
                    lines.map {
                        OrderLineEntity(confirmation.orderId, it.itemId, it.name, it.quantity, it.salePriceCents(), it.unit, it.imageUrl)
                    },
                )
                database.cartDao().clear()
            }
            PlaceOrderResult.Placed(confirmation)
        } catch (e: HttpException) {
            PlaceOrderResult.Rejected(serverMessage(e) ?: GENERIC_REJECTION)
        } catch (e: IOException) {
            PlaceOrderResult.Offline
        } catch (e: SerializationException) {
            PlaceOrderResult.Rejected(GENERIC_REJECTION)
        }
    }

    override fun observeOrders(): Flow<List<Order>> = orderDao.observeAll().map { list -> list.map { it.asExternalModel() } }

    override fun observeOrder(orderId: String): Flow<Order?> = orderDao.observe(orderId).map { it?.asExternalModel() }

    override suspend fun refreshStatus(orderId: String): Boolean = try {
        val status = api.getOrderStatus(orderId).asExternalModel()
        orderDao.updateStatus(orderId, status.name)
        true
    } catch (e: Exception) {
        if (e.isRecoverableNetworkError()) false else throw e
    }

    override suspend fun reorder(orderId: String) {
        val order = orderDao.get(orderId) ?: return
        val now = clock.millis()
        database.cartDao().replaceAll(
            order.lines.mapIndexed { index, line ->
                CartItemEntity(
                    itemId = line.itemId,
                    storeId = order.order.storeId,
                    name = line.name,
                    description = "",
                    section = "",
                    priceCents = line.unitPriceCents,
                    discountPercent = 0.0,
                    unit = line.unit,
                    imageUrl = line.imageUrl,
                    quantity = line.quantity,
                    addedAtMillis = now + index,
                )
            },
        )
    }

    private fun serverMessage(e: HttpException): String? = runCatching {
        e.response()?.errorBody()?.string()?.let { json.decodeFromString<NetworkError>(it).message }
    }.getOrNull()

    private fun CartItemEntity.salePriceCents(): Long =
        com.sravanthi.quickcart.core.model.applyDiscount(priceCents, discountPercent)

    private companion object {
        const val EMPTY_CART = "Your cart is empty"
        const val GENERIC_REJECTION = "The store couldn't accept this order. Please try again."
    }
}
