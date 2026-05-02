package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.GetUsersOrdersResponse

data class GetUsersOrdersState(
    val isLoading: Boolean = false,
    val success: GetUsersOrdersResponse? = null,
    val error: String? = null
)