plugins {
    alias(libs.plugins.quickcart.android.library)
}

android {
    namespace = "com.sravanthi.quickcart.core.testing"
}

dependencies {
    api(projects.core.data)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
