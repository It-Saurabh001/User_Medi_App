package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.PasswordResetOtpResponse

data class PasswordResetOtpState(
    val isLoading: Boolean = false,
    val success: PasswordResetOtpResponse? = null,
    val error: String? = null
)