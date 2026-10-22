import com.laarasoft.moepay.convention.enableAndroidResources
import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import com.laarasoft.moepay.convention.pathToSourcePackage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

/** Kotlin Multiplatform library with Compose Multiplatform UI and resources. */
class CmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.laarasoft.moepay.convention.kmp.library")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            enableAndroidResources()

            // Res class lives next to the module's code: com.laarasoft.moepay.core.designsystem.resources.Res
            (extensions.getByType<ComposeExtension>() as ExtensionAware).extensions.configure<ResourcesExtension> {
                publicResClass = true
                packageOfResClass = "${pathToSourcePackage()}.resources"
            }

            dependencies {
                "commonMainImplementation"(libs.library("compose-runtime"))
                "commonMainImplementation"(libs.library("compose-foundation"))
                "commonMainImplementation"(libs.library("compose-ui"))
                "commonMainImplementation"(libs.library("compose-material3"))
                "commonMainImplementation"(libs.library("compose-components-resources"))
                "commonMainImplementation"(libs.library("compose-uiToolingPreview"))

                "androidRuntimeClasspath"(libs.library("compose-uiTooling"))
            }
        }
    }
}
