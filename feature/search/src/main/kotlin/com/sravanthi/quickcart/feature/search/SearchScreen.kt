package com.sravanthi.quickcart.feature.search

import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.quickcart.core.ui.MenuItemRow
import com.sravanthi.quickcart.core.ui.StoreCard

@Composable
internal fun SearchRoute(onBack: () -> Unit, onStoreClick: (Long) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(query, uiState, viewModel::onQueryChange, onBack, onStoreClick)
}

private val suggestions = listOf("Pizza", "Biryani", "Ramen", "Tacos", "Milk", "Smoothie", "Tiramisu")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onStoreClick: (Long) -> Unit,
    autoFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_search_back)) }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text(stringResource(R.string.feature_search_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Close, stringResource(R.string.feature_search_clear))
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).testTag("search_field"),
                    )
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                SearchUiState.Idle -> Suggestions(onQueryChange)
                SearchUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is SearchUiState.Results -> Results(uiState, onStoreClick)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Suggestions(onPick: (String) -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.feature_search_popular), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion -> AssistChip(onClick = { onPick(suggestion) }, label = { Text(suggestion) }) }
        }
    }
}

@Composable
private fun Results(state: SearchUiState.Results, onStoreClick: (Long) -> Unit) {
    if (state.results.isEmpty) {
        Text(
            stringResource(R.string.feature_search_no_results, state.query),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp),
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.testTag("search_results")) {
        if (state.fromCache) item { OfflineNotice() }
        if (state.results.stores.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.feature_search_stores)) }
            items(state.results.stores, key = { "store-${it.id}" }) { store ->
                // Open/closed isn't shown in search; the store page has the live status.
                StoreCard(store, isOpen = true, onClick = { onStoreClick(store.id) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }
        if (state.results.items.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.feature_search_items)) }
            items(state.results.items, key = { "item-${it.item.id}" }) { hit ->
                MenuItemRow(hit.item, storeName = hit.storeName, onClick = { onStoreClick(hit.item.storeId) })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun OfflineNotice() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, Modifier.size(18.dp))
            Text(
                stringResource(R.string.feature_search_offline),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
