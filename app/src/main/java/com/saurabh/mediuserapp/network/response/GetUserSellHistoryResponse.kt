package com.saurabh.mediuserapp.network.response

data class GetUserSellHistoryResponse(
    val message: String,
    val sell_history: List<SellHistory>,
    val status: Int
)