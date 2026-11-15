package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.error.AppError

data class CreateUserState(
    val isLoading: Boolean = false,
    val success: UserDto? = null,
    val error: String? = null,
    val appError: AppError? = null
)
