import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.laarasoft.moepay.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.laarasoft.moepay.convention.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "com.laarasoft.moepay.convention.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "com.laarasoft.moepay.convention.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "com.laarasoft.moepay.convention.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("cmpApplication") {
            id = "com.laarasoft.moepay.convention.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("room") {
            id = "com.laarasoft.moepay.convention.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("appConfig") {
            id = "com.laarasoft.moepay.convention.app.config"
            implementationClass = "AppConfigConventionPlugin"
        }
    }
}
