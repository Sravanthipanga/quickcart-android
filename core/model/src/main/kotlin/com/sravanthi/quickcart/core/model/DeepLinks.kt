package com.sravanthi.quickcart.core.model

/**
 * QuickCart's deep links. Notifications, the navigation graph and the manifest all use these,
 * so a link built in one place always opens in the other.
 */
object DeepLinks {
    const val SCHEME = "quickcart"

    /** The order history. */
    const val ORDERS = "$SCHEME://orders"

    /** Pattern for one order's tracking screen. */
    const val ORDER_PATTERN = "$ORDERS/{orderId}"

    /** Pattern for a store's menu or aisles. */
    const val STORE_PATTERN = "$SCHEME://store/{storeId}"

    fun order(orderId: String): String = "$ORDERS/$orderId"

    fun store(storeId: Long): String = "$SCHEME://store/$storeId"
}
