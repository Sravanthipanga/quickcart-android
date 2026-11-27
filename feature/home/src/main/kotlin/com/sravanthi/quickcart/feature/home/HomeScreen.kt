package com.sravanthi.quickcart.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.IconButton
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.StoreType
import com.sravanthi.quickcart.core.ui.StoreCard
import java.time.LocalTime

@Composable
internal fun HomeRoute(
    onStoreClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val cartItemCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    HomeScreen(
        cartItemCount = cartItemCount,
        onCartClick = onCartClick,
        stores = viewModel.stores.collectAsLazyPagingItems(),
        selectedType = selectedType,
        now = viewModel.currentTime(),
        onTypeSelected = viewModel::selectType,
        onStoreClick = onStoreClick,
        onSearchClick = onSearchClick,
    )
}

/** Filter chips in display order; null means "All". */
private val filters: List<Pair<StoreType?, Int>> = listOf(
    null to R.string.feature_home_filter_all,
    StoreType.RESTAURANT to R.string.feature_home_filter_restaurants,
    StoreType.GROCERY to R.string.feature_home_filter_groceries,
    StoreType.CONVENIENCE to R.string.feature_home_filter_convenience,
    StoreType.DESSERT to R.string.feature_home_filter_desserts,
    StoreType.DRINKS to R.string.feature_home_filter_drinks,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    stores: LazyPagingItems<Store>,
    cartItemCount: Int = 0,
    onCartClick: () -> Unit = {},
    selectedType: StoreType?,
    now: LocalTime,
    onTypeSelected: (StoreType?) -> Unit,
    onStoreClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val refresh = stores.loadState.refresh
    val hasItems = stores.itemCount > 0

    // Offline-first: when a refresh fails but cached stores exist, keep showing them and say so.
    LaunchedEffect(refresh) {
        if (refresh is LoadState.Error && hasItems) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.feature_home_offline),
                actionLabel = context.getString(R.string.feature_home_retry),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) stores.retry()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.feature_home_title))
                        Text(
                            stringResource(R.string.feature_home_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = { CartButton(cartItemCount, onCartClick) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading && hasItems,
            onRefresh = stores::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { SearchBarButton(onSearchClick) }
                item { FilterChips(selectedType, onTypeSelected) }
                when {
                    refresh is LoadState.Loading && !hasItems -> item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    refresh is LoadState.Error && !hasItems -> item { ErrorState(onRetry = stores::retry) }
                    refresh is LoadState.NotLoading && !hasItems -> item {
                        Text(
                            stringResource(R.string.feature_home_no_stores),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                        )
                    }
                }
                items(count = stores.itemCount, key = stores.itemKey { it.id }) { index ->
                    stores[index]?.let { store ->
                        StoreCard(
                            store = store,
                            isOpen = store.isOpenAt(now),
                            onClick = { onStoreClick(store.id) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                when (stores.loadState.append) {
                    is LoadState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(32.dp))
                        }
                    }
                    is LoadState.Error -> item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            OutlinedButton(onClick = stores::retry) { Text(stringResource(R.string.feature_home_load_more_failed)) }
                        }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun CartButton(count: Int, onClick: () -> Unit) {
    val description = pluralStringResource(R.plurals.feature_home_cart, count, count)
    IconButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = description }) {
        BadgedBox(badge = { if (count > 0) Badge { Text(count.toString()) } }) {
            Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
        }
    }
}

@Composable
private fun SearchBarButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { role = Role.Button },
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.feature_home_search_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun FilterChips(selected: StoreType?, onSelected: (StoreType?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters, key = { it.first?.name ?: "ALL" }) { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.CloudOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.feature_home_error_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feature_home_error_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.feature_home_retry)) }
    }
}
