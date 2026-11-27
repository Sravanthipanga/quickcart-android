plugins {
    alias(libs.plugins.quickcart.android.application)
    alias(libs.plugins.quickcart.android.compose)
    alias(libs.plugins.quickcart.hilt)
    alias(libs.plugins.baselineprofile)
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
        // The baseline profile plugin adds release-like build types for profiling and benchmarks.
        // Sign them with the debug key so they install on test devices; real release signing is separate.
        configureEach {
            if (name.startsWith("benchmark") || name.startsWith("nonMinified")) {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
}

baselineProfile {
    // Profiles are generated on demand (see .github/workflows/performance.yml) and committed.
    automaticGenerationDuringBuild = false
    // Also lay out the DEX files so startup code sits together.
    dexLayoutOptimization = true
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.notifications)
    implementation(projects.feature.home)
    implementation(projects.feature.store)
    implementation(projects.feature.search)
    implementation(projects.feature.cart)
    implementation(projects.feature.orders)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)

    baselineProfile(projects.baselineprofile)
}
