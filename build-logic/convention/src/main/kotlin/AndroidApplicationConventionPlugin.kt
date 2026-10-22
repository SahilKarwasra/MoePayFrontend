import com.android.build.api.dsl.ApplicationExtension
import com.laarasoft.moepay.convention.intVersion
import com.laarasoft.moepay.convention.libs
import com.laarasoft.moepay.convention.library
import com.laarasoft.moepay.convention.version
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Android application with Compose (AGP 9 built-in Kotlin). */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                namespace = libs.version("projectApplicationId")
                compileSdk = libs.intVersion("projectCompileSdkVersion")

                defaultConfig {
                    applicationId = libs.version("projectApplicationId")
                    minSdk = libs.intVersion("projectMinSdkVersion")
                    targetSdk = libs.intVersion("projectTargetSdkVersion")
                    versionCode = libs.intVersion("projectVersionCode")
                    versionName = libs.version("projectVersionName")
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                "debugImplementation"(libs.library("compose-uiTooling"))
            }
        }
    }
}
