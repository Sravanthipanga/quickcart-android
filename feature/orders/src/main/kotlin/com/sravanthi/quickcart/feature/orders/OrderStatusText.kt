package com.sravanthi.quickcart.feature.orders

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.sravanthi.quickcart.core.model.OrderStatus

/** Short label for a status chip or timeline step. */
@Composable
internal fun statusLabel(status: OrderStatus): String = stringResource(
    when (status) {
        OrderStatus.PLACED -> R.string.feature_orders_status_placed
        OrderStatus.PREPARING -> R.string.feature_orders_status_preparing
        OrderStatus.PICKED_UP -> R.string.feature_orders_status_picked_up
        OrderStatus.ON_THE_WAY -> R.string.feature_orders_status_on_the_way
        OrderStatus.DELIVERED -> R.string.feature_orders_status_delivered
    },
)

/** A sentence for the tracking headline. */
@Composable
internal fun statusHeadline(status: OrderStatus, storeName: String): String = when (status) {
    OrderStatus.PLACED -> stringResource(R.string.feature_orders_headline_placed, storeName)
    OrderStatus.PREPARING -> stringResource(R.string.feature_orders_headline_preparing, storeName)
    OrderStatus.PICKED_UP -> stringResource(R.string.feature_orders_headline_picked_up)
    OrderStatus.ON_THE_WAY -> stringResource(R.string.feature_orders_headline_on_the_way)
    OrderStatus.DELIVERED -> stringResource(R.string.feature_orders_headline_delivered)
}

@Composable
internal fun statusColor(status: OrderStatus): Color =
    if (status == OrderStatus.DELIVERED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
