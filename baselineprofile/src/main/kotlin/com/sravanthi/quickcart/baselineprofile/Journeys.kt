package com.sravanthi.quickcart.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** Package of the app under test (the benchmark build type has no suffix). */
const val PACKAGE_NAME = "com.sravanthi.quickcart"

private const val TIMEOUT_MS = 10_000L

/**
 * The paths users take most: these are profiled so they run compiled from the first launch,
 * and measured by the benchmarks. Each step waits for the screen it needs, so slow devices
 * don't cause flaky failures.
 */
fun MacrobenchmarkScope.waitForStores() {
    check(device.wait(Until.hasObject(By.res("store_card")), TIMEOUT_MS)) { "Store list didn't load" }
}

fun MacrobenchmarkScope.scrollStores() {
    val list = device.findObject(By.res("store_list"))
    // Keep the fling away from the edge so it doesn't trigger system gestures.
    list.setGestureMargin(device.displayWidth / 5)
    repeat(2) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    list.fling(Direction.UP)
    device.waitForIdle()
}

fun MacrobenchmarkScope.openFirstStoreAndScrollMenu() {
    device.findObject(By.res("store_card")).click()
    check(device.wait(Until.hasObject(By.res("menu_list")), TIMEOUT_MS)) { "Store menu didn't open" }
    val menu = device.findObject(By.res("menu_list"))
    menu.setGestureMargin(device.displayWidth / 5)
    menu.fling(Direction.DOWN)
    device.waitForIdle()
    device.pressBack()
    waitForStores()
}

fun MacrobenchmarkScope.searchFor(query: String) {
    device.findObject(By.res("search_button")).click()
    check(device.wait(Until.hasObject(By.res("search_field")), TIMEOUT_MS)) { "Search didn't open" }
    device.findObject(By.res("search_field")).text = query
    check(device.wait(Until.hasObject(By.res("search_results")), TIMEOUT_MS)) { "No search results" }
    device.pressBack() // hide the keyboard
    device.pressBack()
    waitForStores()
}
