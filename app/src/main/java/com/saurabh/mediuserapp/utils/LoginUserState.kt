package com.saurabh.mediuserapp.utils

import com.saurabh.mediuserapp.network.response.GetSpecificUserResponse
import com.saurabh.mediuserapp.network.response.LoginUserResponse

data class LoginUserState(
    val isLoading: Boolean = false,
    val success: LoginUserResponse? = null,
    val error: String? = null
)

data class UserState(
    val isLoading: Boolean = false,
    val success: GetSpecificUserResponse? = null,
    val error: String? = null
)


