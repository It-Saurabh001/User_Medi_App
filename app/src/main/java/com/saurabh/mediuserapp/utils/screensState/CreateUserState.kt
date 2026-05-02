package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.CreateUserResponse

data class CreateUserState(
    val isLoading: Boolean = false,
    val success: CreateUserResponse? = null,
    val error: String? = null
)