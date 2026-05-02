package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.GetOrderByIdResponse

data class GetOrderByIdState(
    val isLoading: Boolean = false,
    val success: GetOrderByIdResponse? = null,
    val error: String? = null
)