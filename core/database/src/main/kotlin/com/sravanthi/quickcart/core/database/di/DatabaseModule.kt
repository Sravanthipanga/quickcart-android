package com.sravanthi.quickcart.core.database.di

import android.content.Context
import androidx.room.Room
import com.sravanthi.quickcart.core.database.QuickCartDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuickCartDatabase =
        Room.databaseBuilder(context, QuickCartDatabase::class.java, "quickcart.db").build()
}
