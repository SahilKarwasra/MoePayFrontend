import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Feature presentation module: Compose UI + ViewModels + Koin + type-safe navigation. */
class CmpFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.laarasoft.moepay.convention.cmp.library")
            }

            dependencies {
                "commonMainImplementation"(project(":core:domain"))
                "commonMainImplementation"(project(":core:presentation"))
                "commonMainImplementation"(project(":core:designsystem"))

                "commonMainImplementation"(libs.library("koin-core"))
                "commonMainImplementation"(libs.library("koin-compose"))
                "commonMainImplementation"(libs.library("koin-compose-viewmodel"))

                "commonMainImplementation"(libs.library("jetbrains-lifecycle-viewmodel-compose"))
                "commonMainImplementation"(libs.library("jetbrains-lifecycle-runtime-compose"))
                "commonMainImplementation"(libs.library("jetbrains-navigation-compose"))
            }
        }
    }
}
