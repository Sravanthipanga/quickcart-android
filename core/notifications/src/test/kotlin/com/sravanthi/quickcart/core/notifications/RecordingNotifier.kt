package com.sravanthi.quickcart.core.notifications

import com.sravanthi.quickcart.core.model.Order

class RecordingNotifier : OrderNotifier {
    val notified = mutableListOf<Order>()
    override fun notifyStatus(order: Order) {
        notified += order
    }
}
