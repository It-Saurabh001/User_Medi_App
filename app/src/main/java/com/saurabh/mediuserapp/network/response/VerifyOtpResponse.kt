package com.saurabh.mediuserapp.network.response


data class VerifyOtpResponse(
    val status: Int,
    val message: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val role: String? = null
)