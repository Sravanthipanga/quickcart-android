package com.sravanthi.quickcart.core.data.di

import com.sravanthi.quickcart.core.data.repository.CartRepository
import com.sravanthi.quickcart.core.data.repository.NetworkOrderRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstCartRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstSearchRepository
import com.sravanthi.quickcart.core.data.repository.OrderRepository
import com.sravanthi.quickcart.core.data.repository.OfflineFirstStoreRepository
import com.sravanthi.quickcart.core.data.repository.SearchRepository
import com.sravanthi.quickcart.core.data.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindStoreRepository(repository: OfflineFirstStoreRepository): StoreRepository

    @Binds
    abstract fun bindSearchRepository(repository: OfflineFirstSearchRepository): SearchRepository

    @Binds
    abstract fun bindCartRepository(repository: OfflineFirstCartRepository): CartRepository

    @Binds
    abstract fun bindOrderRepository(repository: NetworkOrderRepository): OrderRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
