package com.laarasoft.moepay.core.data.networking

/** Backend origin for the current platform, from the generated AppConfig. */
internal expect val platformBaseUrl: String

object UrlConstants {
    val BASE_URL: String = platformBaseUrl.trimEnd('/')
    const val API_PATH = "/api/v1"

    /** Endpoints called without a bearer token; a 401 from them never triggers a refresh. */
    val UNAUTHENTICATED_PATHS = listOf(
        "/auth/send-otp",
        "/auth/verify-otp",
        "/auth/refresh",
    )
}

fun constructRoute(route: String): String {
    return when {
        route.startsWith("http://") || route.startsWith("https://") -> route
        route.startsWith("/") -> "${UrlConstants.BASE_URL}${UrlConstants.API_PATH}$route"
        else -> "${UrlConstants.BASE_URL}${UrlConstants.API_PATH}/$route"
    }
}

fun isUnauthenticatedPath(encodedPath: String): Boolean =
    UrlConstants.UNAUTHENTICATED_PATHS.any { encodedPath.endsWith(it) }
