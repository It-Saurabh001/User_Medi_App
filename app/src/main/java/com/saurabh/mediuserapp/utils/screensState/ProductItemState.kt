package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.network.response.ProductItem

data class ProductItemState(
    val isLoading: Boolean = false,
    val success: ProductItem? = null,
    val error: String? = null
)