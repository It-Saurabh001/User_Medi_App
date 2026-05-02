package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.CreateOrderResponse

data class CreateOrderState(
    val isLoading: Boolean = false,
    val success: CreateOrderResponse? = null,
    val error: String? = null
)