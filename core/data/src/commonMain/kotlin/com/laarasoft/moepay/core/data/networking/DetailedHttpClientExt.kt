package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

/*
 * Same as the helpers in HttpClientExt.kt, but failures keep `error.details`
 * (PIN attempts remaining, lock expiry, bank decline reason).
 */

suspend inline fun <reified Request, reified Response : Any> HttpClient.postDetailed(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, ApiFailure> = safeCallDetailed {
    post {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        setBody(body)
        builder()
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.putDetailed(
    route: String,
    body: Request,
    crossinline builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, ApiFailure> = safeCallDetailed {
    put {
        url(constructRoute(route))
        setBody(body)
        builder()
    }
}

suspend inline fun <reified T> safeCallDetailed(
    noinline execute: suspend () -> HttpResponse,
): Result<T, ApiFailure> {
    // platformSafeCall maps transport exceptions; the HTTP outcome travels inside its Success.
    val outcome = platformSafeCall<Result<T, ApiFailure>>(execute = execute) { response ->
        Result.Success(responseToDetailedResult<T>(response))
    }
    return when (outcome) {
        is Result.Success -> outcome.data
        is Result.Failure -> Result.Failure(ApiFailure(outcome.error))
    }
}

suspend inline fun <reified T> responseToDetailedResult(response: HttpResponse): Result<T, ApiFailure> {
    val status = response.status.value
    if (status in 200..299) {
        return try {
            val data = response.body<ApiResponse<T>>().data
            if (data != null) Result.Success(data) else Result.Failure(ApiFailure(DataError.Remote.SERIALIZATION))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Failure(ApiFailure(DataError.Remote.SERIALIZATION))
        }
    }
    val error = try {
        response.body<ApiResponse<JsonElement>>().error
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
    return Result.Failure(ApiFailure(mapApiError(error?.code, status), error?.details))
}
