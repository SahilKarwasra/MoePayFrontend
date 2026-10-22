plugins {
    alias(libs.plugins.convention.android.application)
}

dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.compose.uiToolingPreview)
}
