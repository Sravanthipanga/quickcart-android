package com.sravanthi.quickcart.feature.store

import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Arrangement
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.Cart
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.formatPrice
import com.sravanthi.quickcart.core.ui.MenuItemRow
import com.sravanthi.quickcart.core.ui.RemoteImage
import com.sravanthi.quickcart.core.ui.StoreMetaRow
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun StoreRoute(onBack: () -> Unit, onViewCart: () -> Unit, viewModel: StoreViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StoreScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onAdd = viewModel::add,
        onSetQuantity = viewModel::setQuantity,
        onViewCart = onViewCart,
        onConfirmSwitch = viewModel::confirmSwitch,
        onDismissSwitch = viewModel::dismissSwitch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StoreScreen(
    uiState: StoreUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAdd: (MenuItem) -> Unit = {},
    onSetQuantity: (MenuItem, Int) -> Unit = { _, _ -> },
    onViewCart: () -> Unit = {},
    onConfirmSwitch: () -> Unit = {},
    onDismissSwitch: () -> Unit = {},
) {
    (uiState as? StoreUiState.Ready)?.pendingSwitch?.let { switch ->
        AlertDialog(
            onDismissRequest = onDismissSwitch,
            title = { Text(stringResource(R.string.feature_store_switch_title)) },
            text = {
                Text(stringResource(R.string.feature_store_switch_body, switch.currentStoreName, uiState.store.name))
            },
            confirmButton = { TextButton(onClick = onConfirmSwitch) { Text(stringResource(R.string.feature_store_switch_confirm)) } },
            dismissButton = { TextButton(onClick = onDismissSwitch) { Text(stringResource(R.string.feature_store_switch_cancel)) } },
        )
    }
    Scaffold(
        bottomBar = {
            val cart = (uiState as? StoreUiState.Ready)?.cart
            if (cart != null && !cart.isEmpty) ViewCartBar(cart, onViewCart)
        },
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is StoreUiState.Ready) Text(uiState.store.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_store_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                StoreUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                StoreUiState.Error -> ErrorState(onRetry, Modifier.align(Alignment.Center))
                is StoreUiState.Ready -> StoreContent(uiState, onRetry, onAdd, onSetQuantity)
            }
        }
    }
}

@Composable
private fun StoreContent(
    state: StoreUiState.Ready,
    onRetry: () -> Unit,
    onAdd: (MenuItem) -> Unit,
    onSetQuantity: (MenuItem, Int) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize().testTag("menu_list")) {
        item { StoreHeader(state.store, state.isOpen) }
        if (state.isRefreshing) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (state.isOffline) item { OfflineBanner(onRetry) }
        state.sections.forEach { (section, items) ->
            item(key = "section-$section") {
                Text(
                    section,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
                        .semantics { heading() },
                )
            }
            items(items, key = { it.id }) { item ->
                val quantity = state.cart.quantityOf(item.id)
                MenuItemRow(
                    item = item,
                    quantity = quantity,
                    onIncrease = { if (quantity == 0) onAdd(item) else onSetQuantity(item, quantity + 1) },
                    onDecrease = { onSetQuantity(item, quantity - 1) },
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
private fun StoreHeader(store: Store, isOpen: Boolean) {
    Column {
        RemoteImage(
            url = store.imageUrl,
            placeholder = Icons.Outlined.Storefront,
            placeholderSize = 56.dp,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 7f),
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(store.name, style = MaterialTheme.typography.headlineSmall)
            Text(store.cuisine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StoreMetaRow(store)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                )
                Text(
                    when {
                        store.isOpen24Hours -> stringResource(R.string.feature_store_open_24h)
                        isOpen -> stringResource(R.string.feature_store_open_until, store.closesAt.format(timeFormatter))
                        else -> stringResource(R.string.feature_store_closed_opens, store.opensAt.format(timeFormatter))
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                )
                Text(
                    "· " + stringResource(R.string.feature_store_minimum, formatPrice(store.minimumOrderCents)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ViewCartBar(cart: Cart, onViewCart: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
        Button(
            onClick = onViewCart,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                pluralStringResource(R.plurals.feature_store_view_cart, cart.itemCount, cart.itemCount),
                modifier = Modifier.weight(1f),
            )
            Text(formatPrice(cart.subtotalCents), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, Modifier.size(18.dp))
            Text(
                stringResource(R.string.feature_store_offline),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.feature_store_retry)) }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.CloudOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.feature_store_error_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feature_store_error_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.feature_store_retry)) }
    }
}
