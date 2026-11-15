package com.saurabh.mediuserapp.data.v1.core

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/** Standard v1 success envelope: {"status","message","data"}. */
data class ApiEnvelope<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

/** v1 list envelope with pagination; `summary` only on some list endpoints. */
data class PagedEnvelope<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<T>?,
    @SerializedName("pagination") val pagination: Pagination?,
    @SerializedName("summary") val summary: JsonObject? // unverified: shape per endpoint
)

data class Pagination(
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int
)

/** GET /health and /health/ready (NOT enveloped). */
data class HealthResponse(
    @SerializedName("service") val service: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("checks") val checks: Map<String, String>? // only on /health/ready
)
