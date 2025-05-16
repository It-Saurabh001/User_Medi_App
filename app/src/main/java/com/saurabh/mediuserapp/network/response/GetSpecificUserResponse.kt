package com.saurabh.mediuserapp.network.response

data class GetSpecificUserResponse(
    val message: String,
    val status: Int,
    val user: List<User>
)