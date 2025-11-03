package com.saurabh.mediuserapp.utils

import com.saurabh.mediuserapp.network.response.CreateUserResponse
import com.saurabh.mediuserapp.network.response.LoginUserResponse

data class CreateUserState(
    val isLoading: Boolean = false,
    val success: CreateUserResponse? = null,
    val error: String? = null
)