package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.core.Pagination
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.repo.PagedData

data class AllProductState(
    val isLoading: Boolean = false,
    val success: PagedData<ProductDto>? = null,
    val error: String? = null,
    val appError: AppError? = null,
    val pagination: Pagination? = null,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false
)
