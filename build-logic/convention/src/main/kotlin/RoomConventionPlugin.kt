import androidx.room.gradle.RoomExtension
import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Room KMP with the bundled SQLite driver; apply on top of the KMP library convention. */
class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("androidx.room")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain") {
                    dependencies {
                        api(libs.library("androidx-room-runtime"))
                        api(libs.library("androidx-sqlite-bundled"))
                    }
                }
            }

            dependencies {
                // One KSP configuration per target; Room generates the database implementation.
                listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { configuration ->
                    add(configuration, libs.library("androidx-room-compiler"))
                }
            }
        }
    }
}
