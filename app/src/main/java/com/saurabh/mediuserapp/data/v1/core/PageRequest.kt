package com.saurabh.mediuserapp.data.v1.core

/** v1 list query: limit (max 100, clamped by server), offset, sort, order (asc/desc). */
data class PageRequest(
    val limit: Int = DEFAULT_LIMIT,
    val offset: Int = 0,
    val sort: String? = null,
    val order: String? = null
) {
    fun toQueryMap(): Map<String, String> = buildMap {
        put("limit", limit.toString())
        put("offset", offset.toString())
        sort?.let { put("sort", it) }
        order?.let { put("order", it) }
    }

    /** Next page (offset += limit) while offset < total, else null. */
    fun next(pagination: Pagination?): PageRequest? {
        if (pagination == null) return null
        val nextOffset = pagination.offset + pagination.limit
        return if (nextOffset < pagination.total) copy(offset = nextOffset) else null
    }

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}

fun Pagination.hasMore(): Boolean = offset + limit < total

/**
 * Loads the page after the last one and appends it to [current].
 * Nothing more -> Success(current) unchanged; Failure leaves the caller's list intact.
 */
suspend fun <T> loadMore(
    current: List<T>,
    lastRequest: PageRequest,
    lastPagination: Pagination?,
    fetch: suspend (PageRequest) -> ApiResult<List<T>>
): ApiResult<List<T>> {
    val next = lastRequest.next(lastPagination) ?: return ApiResult.Success(current, lastPagination)
    return when (val r = fetch(next)) {
        is ApiResult.Success -> ApiResult.Success(current + r.data, r.pagination, r.message)
        is ApiResult.Failure -> r
    }
}
