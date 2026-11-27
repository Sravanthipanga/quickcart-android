package com.sravanthi.quickcart.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sravanthi.quickcart.core.model.ItemTag
import com.sravanthi.quickcart.core.model.MenuItem
import com.sravanthi.quickcart.core.model.formatPrice

/** A dish or grocery item: name, description, price (with unit and discount), tags and a thumbnail. */
@Composable
fun MenuItemRow(
    item: MenuItem,
    modifier: Modifier = Modifier,
    storeName: String? = null,
    onClick: (() -> Unit)? = null,
    /** When set, shows an Add button or quantity stepper on the photo. */
    quantity: Int? = null,
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {},
) {
    val price = formatPrice(item.salePriceCents) + (item.unit?.let { " · $it" } ?: "")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = listOfNotNull(item.name, storeName, price).joinToString(", ")
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (storeName != null) {
                Text(storeName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (item.description.isNotBlank()) {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(price, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                if (item.hasDiscount) {
                    Text(
                        formatPrice(item.priceCents),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
            if (item.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.tags.sortedBy { it.ordinal }.forEach { TagChip(it) }
                }
            }
        }
        Box(Modifier.size(width = 104.dp, height = 104.dp)) {
            RemoteImage(
                url = item.imageUrl,
                placeholder = Icons.Outlined.Fastfood,
                placeholderSize = 32.dp,
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(12.dp)),
            )
            if (quantity != null) {
                QuantityStepper(
                    quantity = quantity,
                    itemName = item.name,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun TagChip(tag: ItemTag) {
    val (label, container, content) = when (tag) {
        ItemTag.POPULAR -> Triple(
            R.string.core_ui_tag_popular, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ItemTag.VEGETARIAN -> Triple(
            R.string.core_ui_tag_vegetarian, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer,
        )
        ItemTag.SPICY -> Triple(
            R.string.core_ui_tag_spicy, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer,
        )
    }
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(6.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
