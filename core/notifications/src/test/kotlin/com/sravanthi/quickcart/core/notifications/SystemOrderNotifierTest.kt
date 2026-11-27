package com.sravanthi.quickcart.core.notifications

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.testing.sampleOrders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SystemOrderNotifierTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notifier = SystemOrderNotifier(app)
    private val order = sampleOrders().first { it.id == "QC-1046" }

    private fun posted() = shadowOf(manager).allNotifications

    private fun Notification.title() = extras.getCharSequence(Notification.EXTRA_TITLE).toString()

    @Test
    fun `tapping the notification opens the order's tracking screen`() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        notifier.notifyStatus(order)

        val notification = posted().single()
        assertEquals("Your order is on the way", notification.title())
        assertEquals(SystemOrderNotifier.CHANNEL_ID, notification.channelId)
        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("quickcart://orders/QC-1046", intent.dataString)
        assertEquals(app.packageName, intent.`package`)
    }

    @Test
    fun `each order has one notification, updated in place`() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        notifier.notifyStatus(order)
        notifier.notifyStatus(order.copy(status = OrderStatus.DELIVERED))

        assertEquals("Your order has been delivered", posted().single().title())
    }

    @Test
    fun `nothing is posted without permission`() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        notifier.notifyStatus(order)

        assertTrue(posted().isEmpty())
    }
}
