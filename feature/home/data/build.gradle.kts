plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.feature.home.domain)

            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }
    }
}
