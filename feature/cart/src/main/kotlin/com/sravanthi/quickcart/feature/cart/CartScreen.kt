package com.sravanthi.quickcart.feature.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.quickcart.core.model.CartLine
import com.sravanthi.quickcart.core.model.OrderConfirmation
import com.sravanthi.quickcart.core.model.OrderPricing
import com.sravanthi.quickcart.core.model.formatPrice
import com.sravanthi.quickcart.core.ui.QuantityStepper
import com.sravanthi.quickcart.core.ui.RemoteImage

@Composable
internal fun CartRoute(
    onBack: () -> Unit,
    onBrowseStores: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CartScreen(uiState, onBack, onBrowseStores, viewModel::setQuantity, viewModel::placeOrder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CartScreen(
    uiState: CartUiState,
    onBack: () -> Unit,
    onBrowseStores: () -> Unit,
    onSetQuantity: (itemId: Long, quantity: Int) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    Scaffold(
        topBar = {
            if (uiState !is CartUiState.Placed) {
                TopAppBar(
                    title = { Text(stringResource(R.string.feature_cart_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_cart_back))
                        }
                    },
                )
            }
        },
        bottomBar = { if (uiState is CartUiState.Ready) PlaceOrderBar(uiState, onPlaceOrder) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                CartUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                CartUiState.Empty -> EmptyCart(onBrowseStores, Modifier.align(Alignment.Center))
                is CartUiState.Ready -> CartContent(uiState, onSetQuantity)
                is CartUiState.Placed -> OrderPlaced(uiState.confirmation, onBrowseStores, Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun CartContent(state: CartUiState.Ready, onSetQuantity: (Long, Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Text(
                state.cart.store?.name.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp).semantics { heading() },
            )
        }
        items(state.cart.lines, key = { it.item.id }) { line ->
            CartLineRow(line, onSetQuantity)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        }
        item { PriceSummary(state.pricing, Modifier.padding(16.dp)) }
        if (!state.pricing.meetsMinimum) {
            item {
                Notice(stringResource(R.string.feature_cart_below_minimum, formatPrice(state.pricing.amountToMinimumCents), formatPrice(state.pricing.minimumOrderCents)))
            }
        }
        if (!state.isStoreOpen) item { Notice(stringResource(R.string.feature_cart_store_closed)) }
        state.error?.let { error ->
            item {
                Notice(
                    when (error) {
                        is CheckoutError.Rejected -> error.message
                        CheckoutError.Offline -> stringResource(R.string.feature_cart_offline)
                    },
                    isError = true,
                )
            }
        }
        item {
            Text(
                stringResource(R.string.feature_cart_demo_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun CartLineRow(line: CartLine, onSetQuantity: (Long, Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RemoteImage(line.item.imageUrl, Icons.Outlined.Fastfood, Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)), placeholderSize = 24.dp)
        Column(Modifier.weight(1f)) {
            Text(line.item.name, style = MaterialTheme.typography.titleSmall)
            Text(
                formatPrice(line.item.salePriceCents) + (line.item.unit?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        QuantityStepper(
            quantity = line.quantity,
            itemName = line.item.name,
            onIncrease = { onSetQuantity(line.item.id, line.quantity + 1) },
            onDecrease = { onSetQuantity(line.item.id, line.quantity - 1) },
        )
        Text(
            formatPrice(line.totalCents),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun PriceSummary(pricing: OrderPricing, modifier: Modifier = Modifier) {
    ElevatedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceRow(stringResource(R.string.feature_cart_subtotal), pricing.subtotalCents)
            PriceRow(stringResource(R.string.feature_cart_delivery_fee), pricing.deliveryFeeCents, freeWhenZero = true)
            PriceRow(stringResource(R.string.feature_cart_service_fee), pricing.serviceFeeCents)
            HorizontalDivider()
            PriceRow(stringResource(R.string.feature_cart_total), pricing.totalCents, emphasize = true)
        }
    }
}

@Composable
private fun PriceRow(label: String, cents: Long, emphasize: Boolean = false, freeWhenZero: Boolean = false) {
    val style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = style, fontWeight = if (emphasize) FontWeight.Bold else null, modifier = Modifier.weight(1f))
        Text(
            if (freeWhenZero && cents == 0L) stringResource(R.string.feature_cart_free) else formatPrice(cents),
            style = style,
            fontWeight = if (emphasize) FontWeight.Bold else null,
            color = if (freeWhenZero && cents == 0L) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Notice(text: String, isError: Boolean = false) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PlaceOrderBar(state: CartUiState.Ready, onPlaceOrder: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
        Button(
            onClick = onPlaceOrder,
            enabled = state.canPlaceOrder,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (state.isPlacing) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.feature_cart_place_order, formatPrice(state.pricing.totalCents)))
            }
        }
    }
}

@Composable
private fun EmptyCart(onBrowseStores: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.ShoppingCart, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.feature_cart_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feature_cart_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onBrowseStores) { Text(stringResource(R.string.feature_cart_browse)) }
    }
}

@Composable
private fun OrderPlaced(confirmation: OrderConfirmation, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.CheckCircle, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.feature_cart_placed_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.feature_cart_placed_order, confirmation.orderId, confirmation.storeName),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.feature_cart_placed_eta, confirmation.etaMinMinutes, confirmation.etaMaxMinutes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(R.string.feature_cart_placed_total, formatPrice(confirmation.pricing.totalCents)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onDone) { Text(stringResource(R.string.feature_cart_back_to_stores)) }
    }
}
