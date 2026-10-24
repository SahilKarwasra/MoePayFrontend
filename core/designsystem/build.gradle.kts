plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.presentation)
            implementation(libs.qrose)
        }
        androidMain.dependencies {
            implementation(libs.play.services.code.scanner)
        }
    }
}
