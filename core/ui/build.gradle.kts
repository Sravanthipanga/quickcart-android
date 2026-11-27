plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.android.compose)
}

android {
    namespace = "com.sravanthi.quickcart.core.ui"
}

dependencies {
    api(projects.core.model)
    api(projects.core.designsystem)
    implementation(libs.coil.compose)
}
