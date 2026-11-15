package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

/** Customer profile (POST users, GET/PATCH users/me). No password fields. */
data class UserDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("pin_code") val pinCode: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_approved") val isApproved: Boolean,
    @SerializedName("is_blocked") val isBlocked: Boolean,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("created_at") val createdAt: Instant
)

private val TEXT_TYPE = "text/plain; charset=utf-8".toMediaType()

/** Builds multipart text parts for @PartMap, skipping nulls. */
internal fun textParts(vararg pairs: Pair<String, String?>): Map<String, RequestBody> =
    pairs.mapNotNull { (k, v) -> v?.let { k to it.toRequestBody(TEXT_TYPE) } }.toMap()

/** POST users. Sent as multipart (optional `image` part is passed separately). */
data class UserCreateRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val pinCode: String? = null
) {
    fun toParts() = textParts(
        "name" to name, "email" to email, "password" to password,
        "phone_number" to phoneNumber, "address" to address, "pin_code" to pinCode
    )
}

/** PATCH users/me. Changing email or password requires [currentPassword]. */
data class ProfileUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val pinCode: String? = null,
    val password: String? = null,
    val currentPassword: String? = null
) {
    fun toParts() = textParts(
        "name" to name, "address" to address, "email" to email, "phone_number" to phoneNumber,
        "pin_code" to pinCode, "password" to password, "current_password" to currentPassword
    )
}
