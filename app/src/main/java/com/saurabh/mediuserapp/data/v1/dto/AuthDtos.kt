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
/** POST auth/google (user: tokens directly, no OTP; response is [TokenData]). */
data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("account_type") val accountType: String = "user"
)

data class VerifyOtpRequest(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

/** POST auth/verify-otp, email verification form (no account_id). */
data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

data class VerifyEmailData(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("email_verified") val emailVerified: Boolean
)

/** POST auth/resend-otp (server default account_type is admin, so it is always sent). */
data class ResendOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("account_type") val accountType: String = ACCOUNT_TYPE_USER
)

data class ResendOtpData(
    @SerializedName("otp_expires_in") val otpExpiresIn: Int?,
    @SerializedName("resend_cooldown") val resendCooldown: Int? = null
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
