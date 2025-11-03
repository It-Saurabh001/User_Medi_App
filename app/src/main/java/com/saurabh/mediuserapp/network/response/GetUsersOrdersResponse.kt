package com.saurabh.mediuserapp.network.response

data class GetUsersOrdersResponse(
    val message: String,
    val order: List<Order>,
    val status: Int
)