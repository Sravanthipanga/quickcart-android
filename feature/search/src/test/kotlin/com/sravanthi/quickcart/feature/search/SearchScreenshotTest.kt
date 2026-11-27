package com.sravanthi.quickcart.feature.search

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.model.SearchItem
import com.sravanthi.quickcart.core.model.SearchResults
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
import com.sravanthi.quickcart.core.testing.sampleMenu
import com.sravanthi.quickcart.core.testing.sampleStores
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class SearchScreenshotTest {

    @Test
    fun results() = captureRoboImage("$SCREENSHOT_DIR/search.png") {
        val pizzeria = sampleStores.first { it.id == 1L }
        QuickCartTheme {
            SearchScreen(
                query = "pizza",
                uiState = SearchUiState.Results(
                    query = "pizza",
                    results = SearchResults(
                        stores = listOf(pizzeria),
                        items = sampleMenu.filter { it.name.contains("Pizza") }.map { SearchItem(it, pizzeria.name) },
                    ),
                    fromCache = false,
                ),
                onQueryChange = {},
                onBack = {},
                onStoreClick = {},
                autoFocus = false,
            )
        }
    }
}
