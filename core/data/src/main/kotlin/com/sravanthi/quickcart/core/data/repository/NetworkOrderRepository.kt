package com.sravanthi.quickcart.core.data.repository

import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.model.NetworkError
import com.sravanthi.quickcart.core.network.model.NetworkOrderLine
import com.sravanthi.quickcart.core.network.model.NetworkOrderRequest
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class NetworkOrderRepository @Inject constructor(
    private val api: MarketplaceApi,
    private val database: QuickCartDatabase,
    private val json: Json,
) : OrderRepository {

    override suspend fun placeOrder(): PlaceOrderResult {
        val lines = database.cartDao().getAll()
        if (lines.isEmpty()) return PlaceOrderResult.Rejected(EMPTY_CART)

        return try {
            val confirmation = api.placeOrder(
                NetworkOrderRequest(lines.first().storeId, lines.map { NetworkOrderLine(it.itemId, it.quantity) }),
            )
            database.cartDao().clear()
            PlaceOrderResult.Placed(confirmation.asExternalModel())
        } catch (e: HttpException) {
            PlaceOrderResult.Rejected(serverMessage(e) ?: GENERIC_REJECTION)
        } catch (e: IOException) {
            PlaceOrderResult.Offline
        } catch (e: SerializationException) {
            PlaceOrderResult.Rejected(GENERIC_REJECTION)
        }
    }

    private fun serverMessage(e: HttpException): String? = runCatching {
        e.response()?.errorBody()?.string()?.let { json.decodeFromString<NetworkError>(it).message }
    }.getOrNull()

    private companion object {
        const val EMPTY_CART = "Your cart is empty"
        const val GENERIC_REJECTION = "The store couldn't accept this order. Please try again."
    }
}
