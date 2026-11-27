package com.sravanthi.quickcart.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.quickcart.core.model.Order
import com.sravanthi.quickcart.core.model.formatPrice
import com.sravanthi.quickcart.core.ui.RemoteImage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun OrdersRoute(
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onOpenCart: () -> Unit,
    onBrowseStores: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OrdersScreen(
        uiState = uiState,
        onBack = onBack,
        onOrderClick = onOrderClick,
        onReorder = { id -> viewModel.reorder(id, onOpenCart) },
        onBrowseStores = onBrowseStores,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrdersScreen(
    uiState: OrdersUiState,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onReorder: (String) -> Unit,
    onBrowseStores: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_orders_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_orders_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                OrdersUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                OrdersUiState.Empty -> EmptyOrders(onBrowseStores, Modifier.align(Alignment.Center))
                is OrdersUiState.Ready -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.active.isNotEmpty()) {
                        item { SectionTitle(stringResource(R.string.feature_orders_in_progress)) }
                        items(uiState.active, key = { it.id }) { order ->
                            OrderCard(order, onClick = { onOrderClick(order.id) }, onReorder = null)
                        }
                    }
                    if (uiState.past.isNotEmpty()) {
                        item { SectionTitle(stringResource(R.string.feature_orders_past)) }
                        items(uiState.past, key = { it.id }) { order ->
                            OrderCard(order, onClick = { onOrderClick(order.id) }, onReorder = { onReorder(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
}

private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit, onReorder: (() -> Unit)?) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RemoteImage(order.storeImageUrl, Icons.Outlined.Storefront, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), placeholderSize = 28.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(order.storeName, style = MaterialTheme.typography.titleMedium)
                Text(
                    order.placedAt.atZone(ZoneId.systemDefault()).format(dateFormatter) + " · " + order.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    pluralStringResource(R.plurals.feature_orders_items, order.itemCount, order.itemCount) +
                        " · " + formatPrice(order.pricing.totalCents),
                    style = MaterialTheme.typography.bodyMedium,
                )
                StatusChip(order)
            }
            if (onReorder != null) {
                OutlinedButton(onClick = onReorder) { Text(stringResource(R.string.feature_orders_reorder)) }
            }
        }
    }
}

@Composable
private fun StatusChip(order: Order) {
    Surface(
        color = if (order.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = if (order.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Text(statusLabel(order.status), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
private fun EmptyOrders(onBrowseStores: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.ReceiptLong, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.feature_orders_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feature_orders_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onBrowseStores) { Text(stringResource(R.string.feature_orders_browse)) }
    }
}
