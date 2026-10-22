plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.app.config)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            implementation(libs.bundles.ktor.common)
            implementation(libs.koin.core)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.touchlab.kermit)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
