package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.SellHistory

data class SellHistoryState(
    val isLoading: Boolean = false,
    val success: SellHistory? = null,
    val error: String? = null
)