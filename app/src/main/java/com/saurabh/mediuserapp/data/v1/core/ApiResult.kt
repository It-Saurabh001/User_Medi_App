package com.saurabh.mediuserapp.data.v1.core

import com.google.gson.JsonParseException
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorBody
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

sealed class ApiResult<out T> {
    data class Success<out T>(
        val data: T,
        val pagination: Pagination? = null,
        val message: String? = null
    ) : ApiResult<T>()

    data class Failure(val error: AppError) : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data), pagination, message)
    is ApiResult.Failure -> this
}

/**
 * Wraps a v1 call. When the server returns `data: null` (or 204) Success carries null,
 * so declare T nullable (e.g. ApiEnvelope<Any?>) for such endpoints.
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T> safeApiCall(block: suspend () -> Response<ApiEnvelope<T>>): ApiResult<T> = try {
    val response = block()
    if (response.isSuccessful) {
        val body = response.body()
        ApiResult.Success(body?.data as T, null, body?.message)
    } else {
        ApiResult.Failure(response.toApiError())
    }
} catch (e: Throwable) {
    ApiResult.Failure(e.toAppError())
}

suspend fun <T> safeApiCallPaged(block: suspend () -> Response<PagedEnvelope<T>>): ApiResult<List<T>> = try {
    val response = block()
    if (response.isSuccessful) {
        val body = response.body()
        ApiResult.Success(body?.data.orEmpty(), body?.pagination, body?.message)
    } else {
        ApiResult.Failure(response.toApiError())
    }
} catch (e: Throwable) {
    ApiResult.Failure(e.toAppError())
}

/** Parses an error response (JSON, empty or non-JSON) into AppError.Api. Never throws. */
fun Response<*>.toApiError(): AppError.Api {
    val raw = try { errorBody()?.string() } catch (_: IOException) { null }
    val parsed = ErrorBody.parse(raw)
    val errorCode = parsed?.code?.let { ErrorCode.from(it) } ?: ErrorCode.fromHttpStatus(code())
    return AppError.Api(
        httpStatus = code(),
        code = errorCode,
        message = parsed?.message ?: message().takeIf { it.isNotBlank() },
        fields = parsed?.fields
    )
}

/** Maps a thrown exception to AppError; rethrows coroutine cancellation. */
fun Throwable.toAppError(): AppError = when (this) {
    is CancellationException -> throw this
    is SocketTimeoutException -> AppError.Timeout
    is InterruptedIOException ->
        if (message?.contains("timeout", ignoreCase = true) == true) AppError.Timeout else AppError.Network
    is JsonParseException -> AppError.Unknown(this)
    is EOFException -> AppError.Unknown(this) // empty / truncated 2xx JSON body
    is IOException -> AppError.Network
    else -> AppError.Unknown(this)
}
