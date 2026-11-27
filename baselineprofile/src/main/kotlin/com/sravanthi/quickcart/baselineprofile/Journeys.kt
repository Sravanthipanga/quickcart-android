package com.sravanthi.quickcart.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/** Package of the app under test (the benchmark build type has no suffix). */
const val PACKAGE_NAME = "com.sravanthi.quickcart"

private const val TIMEOUT_MS = 10_000L

/**
 * The paths users take most: these are profiled so they run compiled from the first launch,
 * and measured by the benchmarks.
 *
 * Views are looked up fresh before every action: Compose replaces accessibility nodes as the
 * screen updates (e.g. when the next page of stores loads), so a node found earlier can go stale.
 */
private fun MacrobenchmarkScope.find(tag: String): UiObject2 =
    checkNotNull(device.wait(Until.findObject(By.res(tag)), TIMEOUT_MS)) { "\"$tag\" not on screen" }

private fun MacrobenchmarkScope.fling(tag: String, direction: Direction) {
    val list = find(tag)
    // Keep the fling away from the screen edges so it doesn't trigger system gestures.
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(direction)
    device.waitForIdle()
}

fun MacrobenchmarkScope.waitForStores() {
    find("store_card")
}

fun MacrobenchmarkScope.scrollStores() {
    fling("store_list", Direction.DOWN)
    fling("store_list", Direction.DOWN)
    fling("store_list", Direction.UP)
}

fun MacrobenchmarkScope.openFirstStoreAndScrollMenu() {
    find("store_card").click()
    fling("menu_list", Direction.DOWN)
    device.pressBack()
    waitForStores()
}

fun MacrobenchmarkScope.searchFor(query: String) {
    find("search_button").click()
    find("search_field").text = query
    find("search_results")
    device.pressBack() // hide the keyboard
    device.waitForIdle()
    if (!device.hasObject(By.res("store_list"))) device.pressBack()
    waitForStores()
}
