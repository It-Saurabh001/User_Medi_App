package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.GetUserSellHistoryResponse

data class GetUserSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetUserSellHistoryResponse? = null,
    val error: String? = null
)