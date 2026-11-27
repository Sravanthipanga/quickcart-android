plugins {
    alias(libs.plugins.quickcart.android.library)
    alias(libs.plugins.quickcart.android.room)
    alias(libs.plugins.quickcart.hilt)
}

android {
    namespace = "com.sravanthi.quickcart.core.database"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.room.paging)
}
