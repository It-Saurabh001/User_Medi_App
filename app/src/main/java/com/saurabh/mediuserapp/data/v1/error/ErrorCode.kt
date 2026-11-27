package com.saurabh.mediuserapp.data.v1.error

/** All server codes from response.md section 2, plus UNKNOWN for anything unrecognised. */
enum class ErrorCode {
    VALIDATION_ERROR,
    PRESCRIPTION_REQUIRED,
    UNAUTHORIZED,
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    FORBIDDEN,
    ACCOUNT_BLOCKED,
    ACCOUNT_NOT_APPROVED,
    EMAIL_NOT_VERIFIED,
    EMAIL_DELIVERY_FAILED,
    REQUEST_BLOCKED,
    GOOGLE_ACCOUNT_MISMATCH,
    NOT_FOUND,
    METHOD_NOT_ALLOWED,
    CONFLICT,
    OUT_OF_STOCK,
    PRESCRIPTION_NOT_VERIFIED,
    INVALID_TRANSITION,
    ALREADY_SOLD,
    BATCH_EXPIRED,
    PAYLOAD_TOO_LARGE,
    UNSUPPORTED_MEDIA_TYPE,
    UPGRADE_REQUIRED,
    RATE_LIMITED,
    INTERNAL_ERROR,
    UNKNOWN;

    companion object {
        fun from(value: String?): ErrorCode {
            val v = value?.trim()?.uppercase() ?: return UNKNOWN
            return entries.firstOrNull { it.name == v } ?: UNKNOWN
        }

        /** Fallback when the error body has no `code` (empty / non-JSON body). */
        fun fromHttpStatus(status: Int): ErrorCode = when (status) {
            400 -> VALIDATION_ERROR
            401 -> UNAUTHORIZED
            403 -> FORBIDDEN
            404 -> NOT_FOUND
            405 -> METHOD_NOT_ALLOWED
            409 -> CONFLICT
            413 -> PAYLOAD_TOO_LARGE
            415 -> UNSUPPORTED_MEDIA_TYPE
            426 -> UPGRADE_REQUIRED
            429 -> RATE_LIMITED
            in 500..599 -> INTERNAL_ERROR
            else -> UNKNOWN
        }
    }
}
