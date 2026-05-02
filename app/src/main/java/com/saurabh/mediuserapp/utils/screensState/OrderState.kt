package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.Order

data class OrderState(
    val isLoading: Boolean = false,
    val success: Order? = null,
    val error: String? = null
)