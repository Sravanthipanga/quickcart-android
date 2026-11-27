package com.sravanthi.quickcart.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import com.sravanthi.quickcart.core.network.di.NetworkModule
import com.sravanthi.quickcart.core.network.mock.MockMarketplaceInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException

/**
 * The real API client and mock marketplace with the bundled data, plus an in-memory Room database.
 * Set [offline] to simulate airplane mode.
 */
class TestMarketplace {
    var offline = false
    var requests = 0
        private set

    private val json = NetworkModule.provideJson()

    val api = NetworkModule.createApi(
        baseUrl = "https://api.quickcart.example/v1/",
        client = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    requests++
                    if (offline) throw IOException("airplane mode")
                    chain.proceed(chain.request())
                },
            )
            .addInterceptor(MockMarketplaceInterceptor({ File(MARKETPLACE).readText() }, json, latencyMillis = 0))
            .build(),
        json = json,
    )

    val database: QuickCartDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        QuickCartDatabase::class.java,
    ).allowMainThreadQueries().build()

    private companion object {
        // Unit tests run with :core:data as the working directory.
        const val MARKETPLACE = "../network/src/main/assets/marketplace.json"
    }
}
