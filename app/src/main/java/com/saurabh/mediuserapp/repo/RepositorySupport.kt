package com.saurabh.mediuserapp.repo

import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.data.v1.core.ApiResult
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.core.Pagination
import com.saurabh.mediuserapp.data.v1.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDate

/** Carried inside ResultState.Error; `.error` has code, message and field errors. */
class AppException(val error: AppError) : Exception(error.userMessage())

data class PagedData<T>(val items: List<T>, val pagination: Pagination?)

fun <T> v1Flow(block: suspend () -> ApiResult<T>): Flow<ResultState<T>> = flow {
    emit(ResultState.Loading)
    when (val r = block()) {
        is ApiResult.Success -> emit(ResultState.Success(r.data))
        is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
    }
}

fun <T> v1Paged(block: suspend () -> ApiResult<List<T>>): Flow<ResultState<PagedData<T>>> = flow {
    emit(ResultState.Loading)
    when (val r = block()) {
        is ApiResult.Success -> emit(ResultState.Success(PagedData(r.data, r.pagination)))
        is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
    }
}

/** Query map: drops null/blank values; Instant/LocalDate/Boolean/enum become strings. */
fun queryOf(vararg pairs: Pair<String, Any?>): Map<String, String> = buildMap {
    pairs.forEach { (k, v) ->
        val s = when (v) {
            null -> null
            is OrderStatus -> v.wire
            is Enum<*> -> v.name
            is Instant, is LocalDate, is Boolean -> v.toString()
            else -> v.toString()
        }
        if (!s.isNullOrBlank()) put(k, s)
    }
}

data class DateRange(val from: LocalDate? = null, val to: LocalDate? = null)

data class ProductFilter(
    val q: String? = null,
    val category: String? = null,
    val scheduleType: String? = null,
    val requiresPrescription: Boolean? = null,
    val inStock: Boolean? = null,
    val updatedSince: Instant? = null
) {
    fun toQuery() = queryOf(
        "q" to q, "category" to category, "schedule_type" to scheduleType,
        "requires_prescription" to requiresPrescription, "in_stock" to inStock, "updated_since" to updatedSince
    )
}

data class OrderFilter(
    val status: OrderStatus? = null,
    val dateRange: DateRange? = null,
    val updatedSince: Instant? = null,
    val checkoutId: String? = null
) {
    fun toQuery() = queryOf(
        "status" to status, "from" to dateRange?.from, "to" to dateRange?.to, "updated_since" to updatedSince,
        "checkout_id" to checkoutId
    )
}

/** In-memory file picked by the user (image or prescription). */
class FilePart(val fileName: String, val mimeType: String, val bytes: ByteArray) {
    fun toPart(field: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(field, fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
}

typealias ImagePart = FilePart
