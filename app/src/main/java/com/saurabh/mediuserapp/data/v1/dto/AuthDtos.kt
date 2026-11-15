package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName

const val ACCOUNT_TYPE_USER = "user"

/** POST auth/login */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

data class LoginData(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("otp_expires_in") val otpExpiresIn: Int,
    @SerializedName("otp_required") val otpRequired: Boolean
)

/** POST auth/verify-otp */
data class VerifyOtpRequest(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

/** data of auth/verify-otp and auth/refresh. */
data class TokenData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("role") val role: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("account_id") val accountId: String
)

/** auth/refresh returns the same shape as verify-otp. */
typealias RefreshData = TokenData

/** POST auth/logout (Bearer access token). */
data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String?
)

data class LogoutData(
    @SerializedName("revoked") val revoked: List<String>
)

/** POST auth/password-reset/request */
data class PasswordResetRequest(
    @SerializedName("email") val email: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

data class PasswordResetRequestData(
    @SerializedName("otp_expires_in") val otpExpiresIn: Int
)

/** POST auth/password-reset/confirm -> data: null */
data class PasswordResetConfirmRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)
