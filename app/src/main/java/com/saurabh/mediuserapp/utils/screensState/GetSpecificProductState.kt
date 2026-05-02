package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.GetSpecificProductResponse

data class GetSpecificProductState(
    val isLoading: Boolean = false,
    val success: GetSpecificProductResponse? = null,
    val error: String? = null
)