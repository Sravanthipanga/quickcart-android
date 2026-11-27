package com.sravanthi.quickcart.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

/** One background check of active orders. It schedules the next check only while an order is active. */
class OrderUpdatesWorker(
    context: Context,
    params: WorkerParameters,
    private val checker: OrderUpdateChecker,
    private val scheduler: OrderUpdatesScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (checker.checkActiveOrders()) scheduler.scheduleNextCheck()
        return Result.success()
    }
}

/** Creates [OrderUpdatesWorker] with its dependencies from Hilt. */
class OrderUpdatesWorkerFactory @Inject constructor(
    private val checker: Provider<OrderUpdateChecker>,
    private val scheduler: Provider<OrderUpdatesScheduler>,
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        if (workerClassName == OrderUpdatesWorker::class.java.name) {
            OrderUpdatesWorker(appContext, workerParameters, checker.get(), scheduler.get())
        } else {
            null
        }
}
