plugins {
    alias(libs.plugins.quickcart.android.feature)
    alias(libs.plugins.quickcart.android.screenshots)
}

android {
    namespace = "com.sravanthi.quickcart.feature.catalog"
}

dependencies {
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil.compose)

    testImplementation(libs.androidx.paging.testing)
}
