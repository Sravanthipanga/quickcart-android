package com.sravanthi.quickcart.core.data.di

import com.sravanthi.quickcart.core.data.repository.OfflineFirstSearchRepository
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

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
