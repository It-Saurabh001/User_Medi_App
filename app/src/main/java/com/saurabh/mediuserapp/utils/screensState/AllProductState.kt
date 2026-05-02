package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.GetAllProductResponse

data class AllProductState(
    val isLoading: Boolean = false,
    val success: GetAllProductResponse? = null,
    val error: String? = null
)