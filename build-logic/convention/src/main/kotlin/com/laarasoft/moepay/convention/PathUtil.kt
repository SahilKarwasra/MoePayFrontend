package com.laarasoft.moepay.convention

import org.gradle.api.Project

private const val BASE_PACKAGE = "com.laarasoft.moepay"

/**
 * `:feature:auth:presentation` -> `com.laarasoft.moepay.feature.auth.presentation`.
 * Used as the Android namespace and as the base of the Compose resource class package.
 */
fun Project.pathToPackageName(): String {
    val relative = path
        .replace(':', '.')
        .replace('-', '_')
        .lowercase()
    return "$BASE_PACKAGE$relative"
}

/**
 * Kotlin source package for a module. Feature modules drop the `feature` segment:
 * `:feature:auth:presentation` -> `com.laarasoft.moepay.auth.presentation`.
 */
fun Project.pathToSourcePackage(): String =
    pathToPackageName().replace("$BASE_PACKAGE.feature.", "$BASE_PACKAGE.")
