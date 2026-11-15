package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.PasswordResetRequestData
import com.saurabh.mediuserapp.data.v1.error.AppError

data class PasswordResetState(
    val isLoading: Boolean = false,
    val success: PasswordResetRequestData? = null,
    val error: String? = null,
    val appError: AppError? = null
)
