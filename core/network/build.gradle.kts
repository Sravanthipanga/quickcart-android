plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sravanthi.quickcart.core.network"
    buildFeatures.buildConfig = true
    defaultConfig {
        // The marketplace API. Until a real backend exists, the in-app mock server answers every request.
        buildConfigField("String", "API_BASE_URL", "\"https://api.quickcart.example/v1/\"")
        buildConfigField("boolean", "USE_MOCK_MARKETPLACE", "true")
    }
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

}
