package com.saurabh.mediuserapp.network.response

data class GetSpecificProductResponse(
    val message: String? = null,
    val product: ProductItem? = null,
    val status: Int
)