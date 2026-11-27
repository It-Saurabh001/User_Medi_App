package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.error.AppError

data class EmailVerifyState(
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val verified: Boolean = false,
    val codeSent: Boolean = false,
    val error: String? = null,
    val appError: AppError? = null
)
