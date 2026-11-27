plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.android.compose)
}

android {
    namespace = "com.sravanthi.quickcart.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
