package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.RefreshTokenResponse

data class RefreshTokenState(
    val isLoading: Boolean = false,
    val success: RefreshTokenResponse? = null,
    val error: String? = null
)