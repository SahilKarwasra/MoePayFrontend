package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

/** Platform-specific exception mapping (OkHttp on Android, NSURLSession on iOS). */
expect suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>,
): Result<T, DataError.Remote>

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, DataError.Remote> {
    return safeCall {
        get {
            url(constructRoute(route))
            queryParams.forEach { (key, value) -> parameter(key, value) }
            builder()
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, DataError.Remote> {
    return safeCall {
        post {
            url(constructRoute(route))
            queryParams.forEach { (key, value) -> parameter(key, value) }
            setBody(body)
            builder()
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.put(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, DataError.Remote> {
    return safeCall {
        put {
            url(constructRoute(route))
            queryParams.forEach { (key, value) -> parameter(key, value) }
            setBody(body)
            builder()
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, DataError.Remote> {
    return safeCall {
        delete {
            url(constructRoute(route))
            queryParams.forEach { (key, value) -> parameter(key, value) }
            builder()
        }
    }
}

suspend inline fun <reified T> safeCall(
    noinline execute: suspend () -> HttpResponse,
): Result<T, DataError.Remote> {
    return platformSafeCall(execute = execute) { response ->
        responseToResult(response)
    }
}

/** Unwraps the envelope: 2xx -> `data`, otherwise `error.code` -> [DataError.Remote]. */
suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> {
    val status = response.status.value
    if (status in 200..299) {
        return try {
            val data = response.body<ApiResponse<T>>().data
            if (data != null) Result.Success(data) else Result.Failure(DataError.Remote.SERIALIZATION)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Failure(DataError.Remote.SERIALIZATION)
        }
    }

    val errorCode = try {
        response.body<ApiResponse<JsonElement>>().error?.code
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
    return Result.Failure(mapApiError(errorCode, status))
}
