plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.chat.domain)
            implementation(projects.feature.chat.database)
            implementation(projects.core.data)
            implementation(libs.koin.core)
            implementation(libs.touchlab.kermit)
            implementation(libs.bundles.ktor.common)
        }
    }
}
