package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

/** GET api/v1 */
data class ApiIndex(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String,
    @SerializedName("api_version") val apiVersion: String,
    @SerializedName("links") val links: List<ApiLink>
)

data class ApiLink(
    @SerializedName("rel") val rel: String,
    @SerializedName("href") val href: String,
    @SerializedName("methods") val methods: List<String>
)

/** GET api/v1/app-config */
data class AppConfig(
    @SerializedName("api_prefix") val apiPrefix: String,
    @SerializedName("api_version") val apiVersion: String,
    @SerializedName("docs_enabled") val docsEnabled: Boolean,
    @SerializedName("min_app_version") val minAppVersion: String?,
    @SerializedName("server_time") val serverTime: Instant,
    @SerializedName("enums") val enums: AppConfigEnums,
    @SerializedName("error_codes") val errorCodes: List<String>,
    @SerializedName("otp") val otp: OtpConfig,
    @SerializedName("pagination") val pagination: PaginationConfig,
    @SerializedName("uploads") val uploads: UploadConfig
)

data class AppConfigEnums(
    @SerializedName("account_types") val accountTypes: List<String>,
    @SerializedName("device_platforms") val devicePlatforms: List<String>,
    @SerializedName("dosage_forms") val dosageForms: List<String>,
    @SerializedName("order_statuses") val orderStatuses: List<String>,
    @SerializedName("prescription_schedules") val prescriptionSchedules: List<String>,
    @SerializedName("schedule_types") val scheduleTypes: List<String>,
    @SerializedName("stock_movement_reasons") val stockMovementReasons: List<String>,
    @SerializedName("units") val units: List<String>
)

data class OtpConfig(
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("length") val length: Int,
    @SerializedName("max_attempts") val maxAttempts: Int
)

data class PaginationConfig(
    @SerializedName("default_limit") val defaultLimit: Int,
    @SerializedName("max_limit") val maxLimit: Int
)

data class UploadConfig(
    @SerializedName("allowed_mime_types") val allowedMimeTypes: List<String>,
    @SerializedName("image_mime_types") val imageMimeTypes: List<String>,
    @SerializedName("max_request_bytes") val maxRequestBytes: Long,
    @SerializedName("max_upload_mb") val maxUploadMb: Int
)
