package com.sravanthi.quickcart.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@Composable
internal fun CatalogRoute(
    onProductClick: (Long) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    CatalogScreen(viewModel.products.collectAsLazyPagingItems(), onProductClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogScreen(
    products: LazyPagingItems<com.sravanthi.quickcart.core.model.Product>,
    onProductClick: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val refresh = products.loadState.refresh
    val hasItems = products.itemCount > 0

    // Offline-first: when a refresh fails but cached products exist, keep showing them and say so.
    LaunchedEffect(refresh) {
        if (refresh is LoadState.Error && hasItems) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.feature_catalog_offline),
                actionLabel = context.getString(R.string.feature_catalog_retry),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) products.retry()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_catalog_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading && hasItems,
            onRefresh = products::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                refresh is LoadState.Loading && !hasItems ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                refresh is LoadState.Error && !hasItems ->
                    ErrorState(onRetry = products::retry, modifier = Modifier.align(Alignment.Center))
                else -> ProductGrid(products, onProductClick)
            }
        }
    }
}

@Composable
private fun ProductGrid(
    products: LazyPagingItems<com.sravanthi.quickcart.core.model.Product>,
    onProductClick: (Long) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(count = products.itemCount, key = products.itemKey { it.id }) { index ->
            products[index]?.let { product -> ProductCard(product, onClick = { onProductClick(product.id) }) }
        }
        when (products.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                }
            }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    OutlinedButton(onClick = products::retry) {
                        Text(stringResource(R.string.feature_catalog_load_more_failed))
                    }
                }
            }
            is LoadState.NotLoading -> Unit
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
        Text(stringResource(R.string.feature_catalog_error_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feature_catalog_error_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.feature_catalog_retry)) }
    }
}
