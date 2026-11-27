package com.sravanthi.quickcart.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.OrderStatus
import com.sravanthi.quickcart.core.model.formatPrice
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TrackOrderRoute(onBack: () -> Unit, onOpenCart: () -> Unit, viewModel: TrackOrderViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackOrderScreen(uiState, onBack, onReorder = { viewModel.reorder(onOpenCart) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackOrderScreen(uiState: TrackOrderUiState, onBack: () -> Unit, onReorder: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (uiState as? TrackOrderUiState.Ready)?.let { stringResource(R.string.feature_orders_order_title, it.order.id) }
                            ?: stringResource(R.string.feature_orders_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_orders_back)) }
                },
            )
        },
        bottomBar = {
            val order = (uiState as? TrackOrderUiState.Ready)?.order
            if (order != null && !order.isActive) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Button(
                        onClick = onReorder,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    ) { Text(stringResource(R.string.feature_orders_reorder_all)) }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                TrackOrderUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                TrackOrderUiState.NotFound -> Text(
                    stringResource(R.string.feature_orders_not_found),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                is TrackOrderUiState.Ready -> TrackOrderContent(uiState)
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
private fun TrackOrderContent(state: TrackOrderUiState.Ready) {
    val order = state.order
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { StatusHeader(order) }
        if (state.isOffline && order.isActive) item { OfflineNote() }
        item { Timeline(order.status) }
        item { OrderDetails(order) }
    }
}

@Composable
private fun StatusHeader(order: Order) {
    val arriveBy = order.arriveBy.atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            statusHeadline(order.status, order.storeName),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            if (order.isActive) {
                stringResource(R.string.feature_orders_arrive_by, arriveBy)
            } else {
                stringResource(R.string.feature_orders_delivered_from, order.storeName)
            },
            style = MaterialTheme.typography.titleMedium,
            color = statusColor(order.status),
        )
        val progress = (order.status.ordinal + 1f) / OrderStatus.entries.size
        LinearProgressIndicator(
            progress = { progress },
            color = statusColor(order.status),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
    }
}

@Composable
private fun Timeline(current: OrderStatus) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OrderStatus.entries.forEach { status ->
                val done = status.ordinal < current.ordinal || current == OrderStatus.DELIVERED
                val isCurrent = status == current && current != OrderStatus.DELIVERED
                val state = when {
                    done -> stringResource(R.string.feature_orders_step_done)
                    isCurrent -> stringResource(R.string.feature_orders_step_current)
                    else -> stringResource(R.string.feature_orders_step_upcoming)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) { stateDescription = state },
                ) {
                    StepDot(done, isCurrent)
                    Text(
                        statusLabel(status),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent) FontWeight.Bold else null,
                        color = if (done || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDot(done: Boolean, isCurrent: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .then(
                when {
                    done -> Modifier.background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    isCurrent -> Modifier.background(primary, CircleShape)
                    else -> Modifier.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                },
            ),
    ) {
        if (done) Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onTertiary)
        if (isCurrent) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.onPrimary, CircleShape))
    }
}

@Composable
private fun OfflineNote() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, Modifier.size(18.dp))
            Text(
                stringResource(R.string.feature_orders_offline),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun OrderDetails(order: Order) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(order.storeName, style = MaterialTheme.typography.titleMedium)
            order.lines.forEach { line ->
                Row(Modifier.fillMaxWidth()) {
                    Text("${line.quantity}×", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(32.dp))
                    Text(line.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(formatPrice(line.totalCents), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            PriceRow(stringResource(R.string.feature_orders_subtotal), order.pricing.subtotalCents)
            PriceRow(stringResource(R.string.feature_orders_delivery_fee), order.pricing.deliveryFeeCents)
            PriceRow(stringResource(R.string.feature_orders_service_fee), order.pricing.serviceFeeCents)
            PriceRow(stringResource(R.string.feature_orders_total), order.pricing.totalCents, bold = true)
        }
    }
}

@Composable
private fun PriceRow(label: String, cents: Long, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else null, modifier = Modifier.weight(1f))
        Text(formatPrice(cents), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else null)
    }
}
