package com.sravanthi.quickcart.core.network

import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.mock.MockMarketplaceInterceptor
import com.sravanthi.quickcart.core.network.model.NetworkError
import com.sravanthi.quickcart.core.network.model.NetworkOrderLine
import com.sravanthi.quickcart.core.network.model.NetworkOrderRequest
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import java.io.File
import java.time.LocalTime

class OrderApiTest {

    private val json = NetworkModule.provideJson()
    private var serverTime = LocalTime.of(19, 0)
    private val api = NetworkModule.createApi(
        baseUrl = "https://api.quickcart.example/v1/",
        client = OkHttpClient.Builder()
            .addInterceptor(
                MockMarketplaceInterceptor(
                    loadData = { File("src/main/assets/marketplace.json").readText() },
                    json = json,
                    latencyMillis = 0,
                    now = { serverTime },
                ),
            )
            .build(),
        json = json,
    )

    // Bella Napoli (store 1): Classic Margherita Pizza (1001) is $14.99, Tomato Basil Bruschetta (1006) is $8.99.
    private fun order(vararg lines: Pair<Long, Int>, storeId: Long = 1) =
        NetworkOrderRequest(storeId, lines.map { (id, qty) -> NetworkOrderLine(id, qty) })

    private suspend fun expectError(code: Int, block: suspend () -> Unit): String {
        try {
            block()
        } catch (e: HttpException) {
            assertEquals(code, e.code())
            return json.decodeFromString<NetworkError>(e.response()!!.errorBody()!!.string()).message
        }
        fail("Expected HTTP $code")
        error("unreachable")
    }

    @Test
    fun `a valid order is priced by the server`() = runTest {
        val confirmation = api.placeOrder(order(1001L to 2, 1006L to 1))

        assertTrue(confirmation.orderId.startsWith("QC-"))
        assertEquals(1499L * 2 + 899, confirmation.subtotalCents)
        assertEquals(199L, confirmation.deliveryFeeCents)
        assertEquals(195L, confirmation.serviceFeeCents) // 5% of $38.97
        assertEquals(25, confirmation.etaMinMinutes)
    }

    @Test
    fun `order numbers increase`() = runTest {
        val first = api.placeOrder(order(1001L to 1)).orderId
        val second = api.placeOrder(order(1001L to 1)).orderId
        assertEquals(first.removePrefix("QC-").toInt() + 1, second.removePrefix("QC-").toInt())
    }

    @Test
    fun `below the minimum is rejected`() = runTest {
        val message = expectError(422) { api.placeOrder(order(1006L to 1)) } // $8.99 < $12.00
        assertTrue(message.contains("minimum"))
    }

    @Test
    fun `closed stores do not take orders`() = runTest {
        serverTime = LocalTime.of(8, 0)
        val message = expectError(409) { api.placeOrder(order(1001L to 1)) }
        assertEquals("Bella Napoli Pizzeria is closed right now", message)
    }

    @Test
    fun `items from another store or bad quantities are rejected`() = runTest {
        expectError(400) { api.placeOrder(order(1060L to 1)) } // a FreshMart item
        expectError(400) { api.placeOrder(order(1001L to 0)) }
        expectError(404) { api.placeOrder(order(1001L to 1, storeId = 999)) }
    }
}
