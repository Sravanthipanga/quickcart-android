import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Screenshot tests that render Compose screens on the JVM with Robolectric and Roborazzi.
 * `./gradlew recordRoborazziDebug` writes the images to docs/screenshots; normal test runs
 * still execute them as rendering smoke tests without writing files.
 */
class AndroidScreenshotsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.github.takahirom.roborazzi")

        extensions.configure<LibraryExtension> {
            testOptions.unitTests.isIncludeAndroidResources = true
        }
        dependencies {
            add("testImplementation", libs.lib("robolectric"))
            add("testImplementation", libs.lib("roborazzi"))
            add("testImplementation", libs.lib("roborazzi-compose"))
            add("testImplementation", platform(libs.lib("androidx-compose-bom")))
            add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
            add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
        }
    }
}
