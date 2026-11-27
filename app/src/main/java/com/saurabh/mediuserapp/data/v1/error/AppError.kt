package com.saurabh.mediuserapp.data.v1.error

sealed class AppError {
    data class Api(
        val httpStatus: Int,
        val code: ErrorCode,
        val message: String?,
        val fields: Map<String, String>?
    ) : AppError()

    data object Network : AppError()
    data object Timeout : AppError()
    data class Unknown(val cause: Throwable?) : AppError()

    /** Customer-facing text. Per-input VALIDATION_ERROR messages stay in [Api.fields]. */
    fun userMessage(): String = when (this) {
        Network -> "No internet connection. Please check your network and try again."
        Timeout -> "The server is taking too long to respond. Please try again."
        is Unknown -> "Something went wrong. Please try again."
        is Api -> apiMessage(this)
    }

    private fun apiMessage(e: Api): String = when (e.code) {
        ErrorCode.OUT_OF_STOCK -> e.fields?.get("quantity")
            ?.let { "Only $it left in stock. Please reduce the quantity." }
            ?: "This medicine is not available in the requested quantity."
        ErrorCode.PRESCRIPTION_REQUIRED ->
            "A prescription is required for this medicine. Please upload it and add the doctor's name."
        ErrorCode.ACCOUNT_NOT_APPROVED ->
            "Your account is awaiting approval by the pharmacy. Please try again later."
        ErrorCode.ACCOUNT_BLOCKED -> "Your account has been blocked. Please contact the pharmacy."
        ErrorCode.EMAIL_NOT_VERIFIED -> "Verify your email address to continue."
        ErrorCode.EMAIL_DELIVERY_FAILED -> "We couldn't send the verification email. Please try again shortly."
        ErrorCode.REQUEST_BLOCKED -> "Your request couldn't be processed. Please check your input."
        ErrorCode.RATE_LIMITED -> "Too many attempts. Please wait a moment or request a new code."
        ErrorCode.VALIDATION_ERROR -> e.message ?: "Please check your details and try again."
        ErrorCode.NOT_FOUND -> "We couldn't find what you were looking for."
        ErrorCode.UNAUTHORIZED -> e.message ?: "Invalid email or password."
        ErrorCode.TOKEN_EXPIRED, ErrorCode.TOKEN_INVALID -> "Your session has ended. Please sign in again."
        ErrorCode.FORBIDDEN -> "You don't have permission to do that."
        ErrorCode.CONFLICT, ErrorCode.INVALID_TRANSITION, ErrorCode.ALREADY_SOLD ->
            "This order was updated. Please refresh and try again."
        ErrorCode.PAYLOAD_TOO_LARGE -> "The file is too large. Please choose a smaller file."
        ErrorCode.UNSUPPORTED_MEDIA_TYPE -> "This file type is not supported. Use JPG, PNG, WEBP or PDF."
        ErrorCode.UPGRADE_REQUIRED -> "Please update the app to continue."
        ErrorCode.INTERNAL_ERROR -> "Server error. Please try again shortly."
        else -> e.message ?: "Something went wrong. Please try again."
    }
}
