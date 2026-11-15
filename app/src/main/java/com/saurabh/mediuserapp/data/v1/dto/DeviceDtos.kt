package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class DeviceDto(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("app_version") val appVersion: String?, // unverified: optional on request
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: Instant,
    @SerializedName("updated_at") val updatedAt: Instant
)

/** POST devices (upsert on token). platform: android | ios. */
data class DeviceRegisterRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android",
    @SerializedName("app_version") val appVersion: String? = null
)
