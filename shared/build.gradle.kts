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
            implementation(projects.core.ui)

            implementation(projects.feature.auth.domain)
            implementation(projects.feature.auth.presentation)

            implementation(projects.feature.onboarding.domain)
            implementation(projects.feature.onboarding.data)
            implementation(projects.feature.onboarding.presentation)

            implementation(projects.feature.home.domain)
            implementation(projects.feature.home.data)
            implementation(projects.feature.home.presentation)

            implementation(projects.feature.wallet.domain)
            implementation(projects.feature.wallet.data)
            implementation(projects.feature.wallet.presentation)

            implementation(projects.feature.payments.domain)
            implementation(projects.feature.payments.data)
            implementation(projects.feature.payments.presentation)

            implementation(projects.feature.activity.domain)
            implementation(projects.feature.activity.data)
            implementation(projects.feature.activity.presentation)

            implementation(projects.feature.chat.domain)
            implementation(projects.feature.chat.data)
            implementation(projects.feature.chat.database)
            implementation(projects.feature.chat.presentation)
        }
    }
}
