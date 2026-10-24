import com.laarasoft.moepay.convention.configureKotlinMultiplatform
import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Pure Kotlin Multiplatform library (domain / data modules). No Compose. */
class KmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()

            dependencies {
                "commonMainImplementation"(libs.library("kotlinx-coroutines-core"))
                "commonMainImplementation"(libs.library("kotlinx-serialization-json"))
                "commonTestImplementation"(libs.library("kotlin-test"))
                "commonTestImplementation"(libs.library("kotlinx-coroutines-test"))
            }
        }
    }
}
