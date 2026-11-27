package com.sravanthi.quickcart.feature.catalog

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
import com.sravanthi.quickcart.core.testing.sampleProducts
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class CatalogScreenshotTest {

    /** A fully loaded catalog, so no refresh indicator is captured. */
    private val settled = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    @Test
    fun catalog() = captureRoboImage("$SCREENSHOT_DIR/catalog.png") {
        QuickCartTheme {
            CatalogScreen(flowOf(PagingData.from(sampleProducts, sourceLoadStates = settled)).collectAsLazyPagingItems(), onProductClick = {})
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun catalogDark() = captureRoboImage("$SCREENSHOT_DIR/catalog_dark.png") {
        QuickCartTheme {
            CatalogScreen(flowOf(PagingData.from(sampleProducts, sourceLoadStates = settled)).collectAsLazyPagingItems(), onProductClick = {})
        }
    }
}
