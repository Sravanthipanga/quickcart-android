package com.sravanthi.quickcart

import android.app.Application
import androidx.work.Configuration
import com.sravanthi.quickcart.core.notifications.OrderUpdatesScheduler
import com.sravanthi.quickcart.core.notifications.OrderUpdatesWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@HiltAndroidApp
class QuickCartApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: OrderUpdatesWorkerFactory

    @Inject lateinit var orderUpdates: OrderUpdatesScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Background order updates run only while an order is active.
        orderUpdates.watch(MainScope())
    }
}
