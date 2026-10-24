package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Error
import kotlinx.serialization.json.JsonObject

/** A failed call with the backend's `error.details`, for errors whose details drive the UI. */
data class ApiFailure(
    val error: DataError.Remote,
    val details: JsonObject? = null,
) : Error
