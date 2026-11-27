pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "quickcart"

include(":app")
include(":baselineprofile")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:ui")
include(":core:notifications")
include(":core:testing")
include(":feature:home")
include(":feature:store")
include(":feature:search")
include(":feature:cart")
include(":feature:orders")
