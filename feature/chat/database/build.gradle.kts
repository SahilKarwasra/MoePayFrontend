plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.chat.domain)
            implementation(libs.koin.core)
        }
    }
}
