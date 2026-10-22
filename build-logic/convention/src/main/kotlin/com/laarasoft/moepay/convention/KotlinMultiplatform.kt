package com.laarasoft.moepay.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Targets: Android (AGP 9 KMP library plugin), iosArm64 (devices) and iosSimulatorArm64.
 * The default hierarchy template gives commonMain, androidMain, iosMain, appleMain, nativeMain.
 */
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
            namespace = pathToPackageName()
            compileSdk = libs.intVersion("projectCompileSdkVersion")
            minSdk = libs.intVersion("projectMinSdkVersion")
        }

        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

        iosArm64()
        iosSimulatorArm64()

        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexpect-actual-classes",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.time.ExperimentalTime",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )
        }
    }
}

/** Enables Android resources so Compose Multiplatform resources are packaged for Android. */
internal fun Project.enableAndroidResources() {
    extensions.configure<KotlinMultiplatformExtension> {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
            androidResources {
                enable = true
            }
        }
    }
}
