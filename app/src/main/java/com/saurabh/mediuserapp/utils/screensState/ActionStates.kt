package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.error.AppError

data class UpdateProfileState(
    val isLoading: Boolean = false,
    val success: UserDto? = null,
    val error: String? = null,
    val appError: AppError? = null
)

data class CancelOrderState(
    val isLoading: Boolean = false,
    val success: OrderDto? = null,
    val error: String? = null,
    val appError: AppError? = null
)

data class UploadPrescriptionState(
    val isLoading: Boolean = false,
    val success: OrderDto? = null,
    val error: String? = null,
    val appError: AppError? = null
)
