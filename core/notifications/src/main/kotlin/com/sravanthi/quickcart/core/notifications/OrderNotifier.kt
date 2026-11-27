package com.sravanthi.quickcart.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sravanthi.quickcart.core.model.DeepLinks
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

/** Tells the user that an order's status changed. */
interface OrderNotifier {
    fun notifyStatus(order: Order)
}

/**
 * Posts one notification per order, updated in place as the order moves. Tapping it opens the
 * order's tracking screen through its deep link. Does nothing if the user hasn't allowed notifications.
 */
class SystemOrderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : OrderNotifier {

    private val manager = NotificationManagerCompat.from(context)

    override fun notifyStatus(order: Order) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!manager.areNotificationsEnabled()) return
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.core_notifications_ic_order)
            .setContentTitle(title(order))
            .setContentText(text(order))
            .setContentIntent(openOrder(order.id))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        // Tagged with the order id so each order has exactly one notification.
        manager.notify(order.id, NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.core_notifications_channel_name))
                .setDescription(context.getString(R.string.core_notifications_channel_description))
                .build(),
        )
    }

    private fun title(order: Order): String = when (order.status) {
        OrderStatus.PLACED -> context.getString(R.string.core_notifications_placed, order.storeName)
        OrderStatus.PREPARING -> context.getString(R.string.core_notifications_preparing, order.storeName)
        OrderStatus.PICKED_UP -> context.getString(R.string.core_notifications_picked_up)
        OrderStatus.ON_THE_WAY -> context.getString(R.string.core_notifications_on_the_way)
        OrderStatus.DELIVERED -> context.getString(R.string.core_notifications_delivered)
    }

    private fun text(order: Order): String = if (order.isActive) {
        val arriveBy = order.arriveBy.atZone(ZoneId.systemDefault()).toLocalTime()
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        context.getString(R.string.core_notifications_arrive_by, order.id, arriveBy)
    } else {
        context.getString(R.string.core_notifications_enjoy, order.id, order.storeName)
    }

    private fun openOrder(orderId: String): PendingIntent {
        // A fresh task with the store list underneath, so Back from tracking lands on Home.
        val intent = Intent(Intent.ACTION_VIEW, DeepLinks.order(orderId).toUri())
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(
            context, orderId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val CHANNEL_ID = "order_updates"
        private const val NOTIFICATION_ID = 1
    }
}
