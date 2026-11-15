package com.saurabh.mediuserapp.data.v1.core

import com.saurabh.mediuserapp.data.v1.error.AppError

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val error: AppError) : UiState<Nothing>
}

/** null data or empty collection -> Empty. */
fun <T> ApiResult<T>.toUiState(): UiState<T> = when (this) {
    is ApiResult.Success -> {
        val d = data
        when {
            d == null -> UiState.Empty
            d is Collection<*> && d.isEmpty() -> UiState.Empty
            else -> UiState.Success(d)
        }
    }
    is ApiResult.Failure -> UiState.Error(error)
}
