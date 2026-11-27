package com.sravanthi.quickcart.core.ui

import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sravanthi.quickcart.core.model.Store
import com.sravanthi.quickcart.core.model.formatPrice
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/** A store on the home list: cover image, name, cuisine, rating, delivery time and fee. */
@Composable
fun StoreCard(store: Store, isOpen: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val meta = storeMeta(store)
    val closedLabel = if (isOpen) null else stringResource(R.string.core_ui_closed_opens_at, store.opensAt.format(timeFormatter))
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("store_card")
            .semantics(mergeDescendants = true) {
                contentDescription = listOfNotNull(store.name, store.cuisine, meta, closedLabel).joinToString(", ")
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 7f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        ) {
            RemoteImage(store.imageUrl, Icons.Outlined.Storefront, Modifier.fillMaxSize(), placeholderSize = 48.dp)
            if (closedLabel != null) {
                Surface(color = Color.Black.copy(alpha = 0.55f), modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(closedLabel, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(store.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                store.cuisine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StoreMetaRow(store, Modifier.padding(top = 4.dp))
        }
    }
}

/** "★ 4.7 (1.3k) · 25–35 min · $1.99 delivery" */
@Composable
fun StoreMetaRow(store: Store, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Filled.Star, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            "${formatRating(store.rating)} (${formatCount(store.ratingCount)})",
            style = MaterialTheme.typography.labelLarge,
        )
        Text("· " + deliveryTime(store), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "· " + deliveryFee(store),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (store.hasFreeDelivery) FontWeight.Bold else null,
            color = if (store.hasFreeDelivery) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun deliveryTime(store: Store): String =
    stringResource(R.string.core_ui_delivery_time, store.deliveryMinMinutes, store.deliveryMaxMinutes)

@Composable
fun deliveryFee(store: Store): String =
    if (store.hasFreeDelivery) {
        stringResource(R.string.core_ui_free_delivery)
    } else {
        stringResource(R.string.core_ui_delivery_fee, formatPrice(store.deliveryFeeCents))
    }

@Composable
private fun storeMeta(store: Store): String = stringResource(
    R.string.core_ui_store_meta,
    formatRating(store.rating),
    deliveryTime(store),
    deliveryFee(store),
)
