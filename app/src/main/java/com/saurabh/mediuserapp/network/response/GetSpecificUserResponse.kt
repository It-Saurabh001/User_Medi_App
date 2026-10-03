package com.saurabh.mediuserapp.network.response

data class GetSpecificUserResponse(
    val message: String? = null,
    val status: Int,
    val user: User? = null
)