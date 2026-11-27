package com.sravanthi.quickcart.core.notifications

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.testing.FakeCartRepository
import com.sravanthi.quickcart.core.testing.FakeOrderRepository
import com.sravanthi.quickcart.core.testing.sampleMenu
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Provider

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OrderUpdatesSchedulerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val cart = FakeCartRepository()
    private val orders = FakeOrderRepository(cart)
    private val notifier = RecordingNotifier()
    private val checker = OrderUpdateChecker(orders, notifier)
    private val scheduler = OrderUpdatesScheduler(context, orders)
    private val factory = OrderUpdatesWorkerFactory(Provider { checker }, Provider { scheduler })

    @Before
    fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(factory)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    private fun scheduled(): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(OrderUpdatesScheduler.WORK_NAME).get()
            .filter { it.state == WorkInfo.State.ENQUEUED }

    private fun placeOrder(): String = runBlocking {
        cart.add(sampleMenu[0])
        orders.placeOrder()
        orders.orders.value.first().id
    }

    private fun runWorker(): ListenableWorker.Result = runBlocking {
        TestListenableWorkerBuilder<OrderUpdatesWorker>(context).setWorkerFactory(factory).build().doWork()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `placing an order starts background checks once`() = runTest(UnconfinedTestDispatcher()) {
        scheduler.watch(backgroundScope)
        assertEquals("nothing to check yet", 0, scheduled().size)

        placeOrder()
        assertEquals(1, scheduled().size)

        scheduler.start()
        assertEquals("already scheduled, not doubled", 1, scheduled().size)
    }

    @Test
    fun `a check schedules the next one while the order is active`() {
        val id = placeOrder()
        orders.serverStatus[id] = OrderStatus.PICKED_UP

        assertEquals(ListenableWorker.Result.success(), runWorker())

        assertEquals(OrderStatus.PICKED_UP, notifier.notified.single().status)
        assertEquals(1, scheduled().size)
    }

    @Test
    fun `checks stop after delivery`() {
        val id = placeOrder()
        orders.serverStatus[id] = OrderStatus.DELIVERED

        runWorker()

        assertEquals(OrderStatus.DELIVERED, notifier.notified.single().status)
        assertEquals(0, scheduled().size)
    }
}
