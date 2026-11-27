package com.sravanthi.quickcart.core.network.mock

import com.sravanthi.quickcart.core.model.priceOrder
import com.sravanthi.quickcart.core.network.model.NetworkError
import com.sravanthi.quickcart.core.network.model.NetworkMenu
import com.sravanthi.quickcart.core.network.model.NetworkMenuItem
import com.sravanthi.quickcart.core.network.model.NetworkOrderConfirmation
import com.sravanthi.quickcart.core.network.model.NetworkOrderRequest
import com.sravanthi.quickcart.core.network.model.NetworkSearchHit
import com.sravanthi.quickcart.core.network.model.NetworkSearchResults
import com.sravanthi.quickcart.core.network.model.NetworkStore
import com.sravanthi.quickcart.core.network.model.NetworkStorePage
import com.sravanthi.quickcart.core.network.model.asExternalModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * A mock marketplace server inside the app: answers [com.sravanthi.quickcart.core.network.MarketplaceApi]
 * requests from bundled data, with realistic latency. Everything above it (Retrofit, the Room cache,
 * paging, offline handling) runs exactly as it would against a real backend, which replaces this by
 * switching the build flag and base URL.
 */
class MockMarketplaceInterceptor(
    private val loadData: () -> String,
    private val json: Json,
    private val latencyMillis: Long = 300,
    /** The server's clock, for opening hours. */
    private val now: () -> LocalTime = { LocalTime.now() },
) : Interceptor {

    @Serializable
    private data class MarketplaceData(val stores: List<NetworkStore>, val items: List<NetworkMenuItem>)

    private val data: MarketplaceData by lazy { json.decodeFromString(loadData()) }
    private val nextOrderNumber = AtomicInteger(FIRST_ORDER_NUMBER)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (latencyMillis > 0) Thread.sleep(latencyMillis)

        // Paths are relative to the API base, e.g. /v1/stores/3/items -> [stores, 3, items].
        val path = request.url.pathSegments.dropWhile { it == "v1" }.filter { it.isNotEmpty() }
        return when {
            path == listOf("stores") -> respond(request, json.encodeToString(storesPage(request)))
            path.size == 2 && path[0] == "stores" -> store(path[1])
                ?.let { respond(request, json.encodeToString(it)) } ?: notFound(request)
            path.size == 3 && path[0] == "stores" && path[2] == "items" -> store(path[1])
                ?.let { respond(request, json.encodeToString(NetworkMenu(data.items.filter { i -> i.storeId == it.id }))) }
                ?: notFound(request)
            path == listOf("search") -> respond(request, json.encodeToString(search(request.url.queryParameter("q").orEmpty())))
            path == listOf("orders") && request.method == "POST" -> placeOrder(request)
            else -> notFound(request)
        }
    }

    private fun storesPage(request: Request): NetworkStorePage {
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val skip = request.url.queryParameter("skip")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return NetworkStorePage(data.stores.drop(skip).take(limit), data.stores.size, skip, limit)
    }

    private fun store(id: String): NetworkStore? = id.toLongOrNull()?.let { storeId -> data.stores.firstOrNull { it.id == storeId } }

    /** Case-insensitive match on store name and cuisine, and on item name and section. Needs 2+ characters. */
    private fun search(query: String): NetworkSearchResults {
        val q = query.trim()
        if (q.length < MIN_QUERY_LENGTH) return NetworkSearchResults(emptyList(), emptyList())
        val stores = data.stores.filter { it.name.contains(q, true) || it.cuisine.contains(q, true) }
        val storeNames = data.stores.associate { it.id to it.name }
        val items = data.items
            .filter { it.name.contains(q, true) || it.section.contains(q, true) }
            .take(MAX_ITEM_RESULTS)
            .map { NetworkSearchHit(it, storeNames.getValue(it.storeId)) }
        return NetworkSearchResults(stores, items)
    }

    /**
     * Validates and prices an order the way a real backend would: prices come from the server's data,
     * never from the client.
     */
    private fun placeOrder(request: Request): Response {
        val body = Buffer().also { request.body?.writeTo(it) }.readUtf8()
        val order = runCatching { json.decodeFromString<NetworkOrderRequest>(body) }.getOrNull()
            ?: return error(request, 400, "Invalid order")
        val store = data.stores.firstOrNull { it.id == order.storeId } ?: return error(request, 404, "Store not found")
        val storeModel = store.asExternalModel()
        if (!storeModel.isOpenAt(now())) return error(request, 409, "${store.name} is closed right now")
        if (order.lines.isEmpty() || order.lines.any { it.quantity <= 0 }) return error(request, 400, "Invalid order")

        val subtotal = order.lines.sumOf { line ->
            val item = data.items.firstOrNull { it.id == line.itemId && it.storeId == store.id }
                ?: return error(request, 400, "An item is no longer available")
            item.asExternalModel().salePriceCents * line.quantity
        }
        val pricing = priceOrder(subtotal, storeModel)
        if (!pricing.meetsMinimum) return error(request, 422, "Orders from ${store.name} need a minimum subtotal")

        val confirmation = NetworkOrderConfirmation(
            orderId = "QC-${nextOrderNumber.getAndIncrement()}",
            storeName = store.name,
            subtotalCents = pricing.subtotalCents,
            deliveryFeeCents = pricing.deliveryFeeCents,
            serviceFeeCents = pricing.serviceFeeCents,
            minimumOrderCents = pricing.minimumOrderCents,
            etaMinMinutes = store.deliveryMinMinutes,
            etaMaxMinutes = store.deliveryMaxMinutes,
        )
        return respond(request, json.encodeToString(confirmation), code = 201)
    }

    private fun error(request: Request, code: Int, message: String) =
        respond(request, json.encodeToString(NetworkError(message)), code)

    private fun respond(request: Request, body: String, code: Int = 200) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code < 300) "OK" else "Error")
        .body(body.toResponseBody(JSON))
        .build()

    private fun notFound(request: Request) = error(request, 404, "Not found")

    companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_ITEM_RESULTS = 40
        private const val FIRST_ORDER_NUMBER = 1042
        private val JSON = "application/json".toMediaType()
    }
}
