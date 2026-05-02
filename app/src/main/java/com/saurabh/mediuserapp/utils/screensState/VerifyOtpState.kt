package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.VerifyOtpResponse

data class VerifyOtpState(
    val isLoading: Boolean = false,
    val success: VerifyOtpResponse? = null,
    val error: String? = null
)