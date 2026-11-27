package com.sravanthi.quickcart.core.network.mock

import com.sravanthi.quickcart.core.network.model.NetworkMenu
import com.sravanthi.quickcart.core.network.model.NetworkMenuItem
import com.sravanthi.quickcart.core.network.model.NetworkSearchHit
import com.sravanthi.quickcart.core.network.model.NetworkSearchResults
import com.sravanthi.quickcart.core.network.model.NetworkStore
import com.sravanthi.quickcart.core.network.model.NetworkStorePage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
) : Interceptor {

    @Serializable
    private data class MarketplaceData(val stores: List<NetworkStore>, val items: List<NetworkMenuItem>)

    private val data: MarketplaceData by lazy { json.decodeFromString(loadData()) }

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

    private fun respond(request: Request, body: String, code: Int = 200) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == 200) "OK" else "Not Found")
        .body(body.toResponseBody(JSON))
        .build()

    private fun notFound(request: Request) = respond(request, """{"message":"Not found"}""", code = 404)

    companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_ITEM_RESULTS = 40
        private val JSON = "application/json".toMediaType()
    }
}
