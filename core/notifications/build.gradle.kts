plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.hilt)
}

android {
    namespace = "com.sravanthi.quickcart.core.notifications"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(libs.androidx.work.runtime)
    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)

    testImplementation(projects.core.testing)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
