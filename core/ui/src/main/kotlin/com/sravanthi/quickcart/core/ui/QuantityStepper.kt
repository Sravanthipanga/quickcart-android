package com.sravanthi.quickcart.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * "Add" when [quantity] is 0, otherwise − n +. Decreasing from 1 shows a bin icon, since it removes the item.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    itemName: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 2.dp,
        modifier = modifier,
    ) {
        if (quantity == 0) {
            IconButton(onClick = onIncrease, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Add, stringResource(R.string.core_ui_add_item, itemName))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    stateDescription = quantity.toString()
                },
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                    if (quantity == 1) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.core_ui_remove_item, itemName), Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Filled.Remove, stringResource(R.string.core_ui_decrease_item, itemName), Modifier.size(18.dp))
                    }
                }
                Text(
                    quantity.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 20.dp),
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Add, stringResource(R.string.core_ui_increase_item, itemName), Modifier.size(18.dp))
                }
            }
        }
    }
}
