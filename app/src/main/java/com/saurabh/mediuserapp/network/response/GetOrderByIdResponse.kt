package com.saurabh.mediuserapp.network.response

data class GetOrderByIdResponse(
    val message: String? = null,
    val order: Order? = null,
    val status: Int
)