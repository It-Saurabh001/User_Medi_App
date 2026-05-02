package com.saurabh.mediuserapp.network.response

data class LoginUserResponse(
    val message: String? = null,
    val role: String? = "user",
    val status: Int,
    val user_id: String
)
