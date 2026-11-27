package com.sravanthi.quickcart.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/** Package of the app under test (the benchmark build type has no suffix). */
const val PACKAGE_NAME = "com.sravanthi.quickcart"

private const val TIMEOUT_MS = 10_000L
private const val ATTEMPTS = 3

/**
 * The paths users take most: these are profiled so they run compiled from the first launch,
 * and measured by the benchmarks.
 *
 * Compose replaces accessibility nodes as the screen updates (for example while the next page
 * of stores loads), so a view found a moment ago can be gone by the time it is touched. Every
 * action therefore finds its view fresh and, if the view went stale mid-gesture, tries again.
 */
private fun MacrobenchmarkScope.find(tag: String): UiObject2 =
    checkNotNull(device.wait(Until.findObject(By.res(tag)), TIMEOUT_MS)) { "\"$tag\" not on screen" }

private fun MacrobenchmarkScope.onView(tag: String, step: String, action: (UiObject2) -> Unit) {
    var lastError: StaleObjectException? = null
    repeat(ATTEMPTS) {
        try {
            action(find(tag))
            device.waitForIdle()
            return
        } catch (e: StaleObjectException) {
            lastError = e
            device.waitForIdle()
        }
    }
    throw IllegalStateException("$step: \"$tag\" kept changing while it was being used", lastError)
}

private fun MacrobenchmarkScope.fling(tag: String, direction: Direction) =
    onView(tag, "fling $tag $direction") { list ->
        // Keep the fling away from the screen edges so it doesn't trigger system gestures.
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(direction)
    }

/** Waits until the first page of stores is on screen and the list has stopped updating. */
fun MacrobenchmarkScope.waitForStores() {
    find("store_card")
    device.waitForIdle()
}

fun MacrobenchmarkScope.scrollStores() {
    fling("store_list", Direction.DOWN)
    fling("store_list", Direction.DOWN)
    scrollStoresToTop()
}

/** Flings up until the search bar (the list's first item) is back on screen. */
private fun MacrobenchmarkScope.scrollStoresToTop() {
    repeat(6) {
        if (device.hasObject(By.res("search_button"))) return
        fling("store_list", Direction.UP)
    }
    check(device.hasObject(By.res("search_button"))) { "Couldn't scroll the store list back to the top" }
}

fun MacrobenchmarkScope.openFirstStoreAndScrollMenu() {
    onView("store_card", "open a store") { it.click() }
    fling("menu_list", Direction.DOWN)
    device.pressBack()
    waitForStores()
}

fun MacrobenchmarkScope.searchFor(query: String) {
    scrollStoresToTop()
    onView("search_button", "open search") { it.click() }
    onView("search_field", "type a search") { it.text = query }
    find("search_results")
    device.pressBack() // hide the keyboard
    device.waitForIdle()
    if (!device.hasObject(By.res("store_list"))) device.pressBack()
    waitForStores()
}
