package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.TokenData
import com.saurabh.mediuserapp.data.v1.error.AppError

data class VerifyOtpState(
    val isLoading: Boolean = false,
    val success: TokenData? = null,
    val error: String? = null,
    val appError: AppError? = null
)
