package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.ProductDto

data class GetSpecificProductState(
    val isLoading: Boolean = false,
    val success: ProductDto? = null,
    val error: String? = null
)
