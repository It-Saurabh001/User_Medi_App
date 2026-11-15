package com.saurabh.mediuserapp.utils

import com.saurabh.mediuserapp.data.v1.dto.LoginData
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.error.AppError

data class LoginUserState(
    val isLoading: Boolean = false,
    val success: LoginData? = null,
    val error: String? = null,
    val appError: AppError? = null
)

data class UserState(
    val isLoading: Boolean = false,
    val success: UserDto? = null,
    val error: String? = null
)
