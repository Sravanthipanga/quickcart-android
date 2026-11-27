plugins {
    alias(libs.plugins.quickcart.android.application)
    alias(libs.plugins.quickcart.android.compose)
    alias(libs.plugins.quickcart.hilt)
}

android {
    namespace = "com.sravanthi.quickcart"

    defaultConfig {
        applicationId = "com.sravanthi.quickcart"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.feature.catalog)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
}
