package com.sravanthi.quickcart.feature.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.core.testing.PHONE_QUALIFIERS
import com.sravanthi.quickcart.core.testing.SCREENSHOT_DIR
import com.sravanthi.quickcart.core.testing.sampleStores
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class HomeScreenshotTest {

    /** A fully loaded list, so no refresh indicator is captured. */
    private val settled = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    // 12:30: most stores are open; Island Grill (from 4 PM) shows as closed.
    private val lunchtime = LocalTime.of(12, 30)

    private fun capture(name: String) = captureRoboImage("$SCREENSHOT_DIR/$name.png") {
        QuickCartTheme {
            HomeScreen(
                stores = flowOf(PagingData.from(sampleStores, sourceLoadStates = settled)).collectAsLazyPagingItems(),
                selectedType = null,
                now = lunchtime,
                onTypeSelected = {},
                onStoreClick = {},
                onSearchClick = {},
            )
        }
    }

    @Test
    fun home() = capture("home")

    @Test
    @Config(qualifiers = "+night")
    fun homeDark() = capture("home_dark")
}
