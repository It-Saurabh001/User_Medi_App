package com.saurabh.mediuserapp.data.v1.api

import com.google.gson.JsonObject
import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.HealthResponse
import com.saurabh.mediuserapp.data.v1.dto.ApiIndex
import com.saurabh.mediuserapp.data.v1.dto.AppConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/** Public meta endpoints (relative to `api/v1/`). */
interface MetaApi {
    @GET(".")
    suspend fun getIndex(): Response<ApiEnvelope<ApiIndex>>

    @GET("app-config")
    suspend fun getAppConfig(): Response<ApiEnvelope<AppConfig>>

    /** Not enveloped; 404 when docs are disabled. */
    @GET("openapi.json")
    suspend fun getOpenApi(): Response<JsonObject>
}

/** /health and /health/ready live outside /api/v1: pass the absolute URL (server root + LIVE or READY). */
interface HealthApi {
    @GET
    suspend fun getHealth(@Url url: String): Response<HealthResponse>

    companion object {
        const val LIVE = "health"
        const val READY = "health/ready"
    }
}
