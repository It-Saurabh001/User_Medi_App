package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.repo.PagedData

/** Sold orders (v1 has no separate sell-history endpoint for customers). */
data class GetUserSellHistoryState(
    val isLoading: Boolean = false,
    val success: PagedData<OrderDto>? = null,
    val error: String? = null
)
