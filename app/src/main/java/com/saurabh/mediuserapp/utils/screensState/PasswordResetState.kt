package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.PasswordResetResponse

data class PasswordResetState(
    val isLoading: Boolean = false,
    val success: PasswordResetResponse? = null,
    val error: String? = null
)