plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)

            implementation(projects.feature.auth.domain)
            implementation(projects.feature.auth.presentation)

            implementation(projects.feature.onboarding.domain)
            implementation(projects.feature.onboarding.data)
            implementation(projects.feature.onboarding.presentation)

            implementation(projects.feature.home.presentation)
        }
    }
}
