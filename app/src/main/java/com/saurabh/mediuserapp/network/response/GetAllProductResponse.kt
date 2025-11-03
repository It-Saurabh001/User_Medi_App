package com.saurabh.mediuserapp.network.response

data class GetAllProductResponse(
    val message: String,
    val products: List<ProductItem>,
    val status: Int
)