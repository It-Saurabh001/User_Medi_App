package com.saurabh.mediuserapp.network.response

data class GetOrderByIdResponse(
    val message: String,
    val order: Order,
    val status: Int
)