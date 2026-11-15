package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.OrderDto

data class GetOrderByIdState(
    val isLoading: Boolean = false,
    val success: OrderDto? = null,
    val error: String? = null
)
