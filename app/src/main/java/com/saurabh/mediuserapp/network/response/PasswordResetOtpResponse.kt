package com.saurabh.mediuserapp.network.response

data class PasswordResetOtpResponse(
    val message: String? = null,
    val status: Int,
    val user_id: String? = null
)