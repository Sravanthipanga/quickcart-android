import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val android: CommonExtension<*, *, *, *, *, *> =
            extensions.findByType(ApplicationExtension::class.java)
                ?: extensions.getByType(LibraryExtension::class.java)
        android.buildFeatures.compose = true

        extensions.configure<ComposeCompilerGradlePluginExtension> {
            // Model classes and java.time are immutable, but live in modules without the Compose
            // compiler, so Compose must be told. Lets screens skip work when equal data re-emits.
            stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))
            // ./gradlew assembleRelease -Pquickcart.composeReports=true writes stability reports
            // to each module's build/compose-reports.
            if (providers.gradleProperty("quickcart.composeReports").orNull == "true") {
                reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
                metricsDestination.set(layout.buildDirectory.dir("compose-reports"))
            }
        }

        dependencies {
            val bom = libs.lib("androidx-compose-bom")
            add("implementation", platform(bom))
            add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
            add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
        }
    }
}
