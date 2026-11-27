plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.hilt)
}

android {
    namespace = "com.sravanthi.quickcart.core.data"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.paging.runtime)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(libs.androidx.room.ktx)
    implementation(libs.retrofit)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.okhttp)
}
