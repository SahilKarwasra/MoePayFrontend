package com.laarasoft.moepay.core.data.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** The MoePay envelope: `{status_code, success, message, data, error?: {code, details}}`. */
@Serializable
data class ApiResponse<T>(
    @SerialName("status_code") val statusCode: Int = 0,
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
    val error: ApiErrorBody? = null,
)

@Serializable
data class ApiErrorBody(
    val code: String,
    val details: JsonObject? = null,
)
