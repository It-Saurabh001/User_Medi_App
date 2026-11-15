package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.error.AppError

/** confirmPasswordReset result (data is null on the server; success = Unit). */
data class PasswordResetOtpState(
    val isLoading: Boolean = false,
    val success: Unit? = null,
    val error: String? = null,
    val appError: AppError? = null
)
