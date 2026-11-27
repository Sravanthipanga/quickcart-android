package com.sravanthi.quickcart.core.network.di

import android.content.Context
import com.sravanthi.quickcart.core.network.BuildConfig
import com.sravanthi.quickcart.core.network.MarketplaceApi
import com.sravanthi.quickcart.core.network.mock.FileOrderStore
import com.sravanthi.quickcart.core.network.mock.MockMarketplaceInterceptor
import java.io.File
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context, json: Json): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
                if (BuildConfig.USE_MOCK_MARKETPLACE) {
                    addInterceptor(
                        MockMarketplaceInterceptor(
                            loadData = { context.assets.open(MARKETPLACE_ASSET).bufferedReader().use { it.readText() } },
                            json = json,
                            orders = FileOrderStore(File(context.filesDir, MOCK_ORDERS_FILE), json),
                        ),
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient, json: Json): MarketplaceApi = createApi(BuildConfig.API_BASE_URL, client, json)

    /** Shared with tests. */
    fun createApi(baseUrl: String, client: OkHttpClient, json: Json): MarketplaceApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(MarketplaceApi::class.java)

    private const val MARKETPLACE_ASSET = "marketplace.json"
    private const val MOCK_ORDERS_FILE = "mock-server/orders.json"
}
