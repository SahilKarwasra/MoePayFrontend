import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * The shared composition root: everything a feature module has, plus the static
 * `Shared` framework consumed by the Xcode project.
 */
class CmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.laarasoft.moepay.convention.cmp.feature")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinNativeTarget>().configureEach {
                    binaries.framework {
                        baseName = "Shared"
                        isStatic = true
                    }
                }
            }

            dependencies {
                "androidMainImplementation"(libs.library("koin-android"))
            }
        }
    }
}
