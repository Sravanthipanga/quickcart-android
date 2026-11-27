package com.sravanthi.quickcart.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** A network image over a tinted placeholder icon, shown while loading or when offline. */
@Composable
fun RemoteImage(
    url: String,
    placeholder: ImageVector,
    modifier: Modifier = Modifier,
    placeholderSize: Dp = 40.dp,
) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Icon(
            placeholder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .size(placeholderSize)
                .align(Alignment.Center),
        )
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}
