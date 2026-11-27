package com.sravanthi.quickcart.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sravanthi.quickcart.core.data.repository.OrderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps order updates coming in the background, but only while an order is active.
 *
 * A real backend would push status changes (e.g. with Firebase Cloud Messaging). The demo server
 * can't push, so the app checks once a minute, only with a network connection, and stops as soon
 * as no order is active. WorkManager keeps this going if the app is closed or the phone restarts.
 */
@Singleton
class OrderUpdatesScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orders: OrderRepository,
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** Starts background checks whenever an order becomes active, e.g. right after checkout. */
    fun watch(scope: CoroutineScope): Job = scope.launch {
        orders.observeOrders()
            .map { list -> list.any { it.isActive } }
            .distinctUntilChanged()
            .filter { it }
            .collect { start() }
    }

    /** Starts checking, unless checks are already scheduled. */
    fun start() = enqueue(ExistingWorkPolicy.KEEP)

    /** Called by the worker itself: runs after the current check finishes. */
    internal fun scheduleNextCheck() = enqueue(ExistingWorkPolicy.APPEND_OR_REPLACE)

    private fun enqueue(policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<OrderUpdatesWorker>()
            .setInitialDelay(CHECK_INTERVAL)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, policy, request)
    }

    companion object {
        const val WORK_NAME = "order-updates"
        val CHECK_INTERVAL: Duration = Duration.ofMinutes(1)
    }
}
