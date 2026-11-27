plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.android.room)
    alias(libs.plugins.quickcart.hilt)
}

android {
    namespace = "com.sravanthi.quickcart.core.database"
    // Migration tests open the exported schemas.
    sourceSets.getByName("test").assets.srcDir("$projectDir/schemas")
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.room.paging)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.robolectric)
}
