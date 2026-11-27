plugins {
    alias(libs.plugins.quickcart.android.feature)
    alias(libs.plugins.quickcart.android.screenshots)
}

android {
    namespace = "com.sravanthi.quickcart.feature.home"
}

dependencies {
    implementation(libs.androidx.paging.compose)

    testImplementation(libs.androidx.paging.testing)
}
