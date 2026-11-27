package com.sravanthi.quickcart.core.data.di

import com.sravanthi.quickcart.core.data.repository.OfflineFirstProductRepository
import com.sravanthi.quickcart.core.data.repository.ProductRepository
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
    abstract fun bindProductRepository(repository: OfflineFirstProductRepository): ProductRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
