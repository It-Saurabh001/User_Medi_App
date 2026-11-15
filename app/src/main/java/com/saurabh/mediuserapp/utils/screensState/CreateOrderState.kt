package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.error.AppError

data class CreateOrderState(
    val isLoading: Boolean = false,
    val success: OrderDto? = null,
    val error: String? = null,
    val appError: AppError? = null,
    /** Idempotency-Key for the current checkout; kept across retries, cleared on success/reset. */
    val idempotencyKey: String? = null
)
