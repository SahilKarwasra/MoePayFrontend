rootProject.name = "MoePayFrontend"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")

include(":core:domain")
include(":core:data")
include(":core:presentation")
include(":core:designsystem")
include(":core:ui")

include(":feature:auth:domain")
include(":feature:auth:presentation")

include(":feature:onboarding:domain")
include(":feature:onboarding:data")
include(":feature:onboarding:presentation")

include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:presentation")

include(":feature:wallet:domain")
include(":feature:wallet:data")
include(":feature:wallet:presentation")

include(":feature:payments:domain")
include(":feature:payments:data")
include(":feature:payments:presentation")

include(":feature:activity:domain")
include(":feature:activity:data")
include(":feature:activity:presentation")

include(":feature:chat:presentation")
