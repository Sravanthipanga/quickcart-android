package com.sravanthi.quickcart.core.notifications.di

import com.sravanthi.quickcart.core.notifications.OrderNotifier
import com.sravanthi.quickcart.core.notifications.SystemOrderNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface NotificationsModule {
    @Binds
    fun bindsOrderNotifier(notifier: SystemOrderNotifier): OrderNotifier
}
