package com.sravanthi.quickcart.core.notifications

import com.sravanthi.quickcart.core.data.repository.OrderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Refreshes every active order and notifies about the ones whose status moved. */
class OrderUpdateChecker @Inject constructor(
    private val orders: OrderRepository,
    private val notifier: OrderNotifier,
) {
    /** Returns true while any order is still active, meaning it should be checked again later. */
    suspend fun checkActiveOrders(): Boolean {
        val active = orders.observeOrders().first().filter { it.isActive }
        for (before in active) {
            // Offline: keep the last status and try again on the next check.
            if (!orders.refreshStatus(before.id)) continue
            val after = orders.observeOrder(before.id).first() ?: continue
            if (after.status != before.status) notifier.notifyStatus(after)
        }
        return orders.observeOrders().first().any { it.isActive }
    }
}
