plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sravanthi.quickcart.core.network"
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"https://dummyjson.com/\"")
    }
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.okhttp.mockwebserver)
}
