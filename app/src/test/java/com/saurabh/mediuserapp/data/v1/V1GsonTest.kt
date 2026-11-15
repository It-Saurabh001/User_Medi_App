package com.saurabh.mediuserapp.data.v1

import com.saurabh.mediuserapp.data.v1.core.ApiResult
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.core.PageRequest
import com.saurabh.mediuserapp.data.v1.core.Pagination
import com.saurabh.mediuserapp.data.v1.core.V1Gson
import com.saurabh.mediuserapp.data.v1.core.loadMore
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class V1GsonTest {

    private data class Holder(val at: Instant?, val day: LocalDate?, val status: OrderStatus?, val code: ErrorCode?)

    private val gson = V1Gson.instance

    @Test
    fun instant_isoZ() {
        val h = gson.fromJson("""{"at":"2026-11-14T17:33:47Z"}""", Holder::class.java)
        assertEquals(Instant.parse("2026-11-14T17:33:47Z"), h.at)
    }

    @Test
    fun instant_dateOnly() {
        val h = gson.fromJson("""{"at":"2025-07-17"}""", Holder::class.java)
        assertEquals(Instant.parse("2025-07-17T00:00:00Z"), h.at)
    }

    @Test
    fun instant_nullBlankInvalid() {
        assertNull(gson.fromJson("""{"at":null}""", Holder::class.java).at)
        assertNull(gson.fromJson("""{"at":""}""", Holder::class.java).at)
        assertNull(gson.fromJson("""{"at":"not-a-date"}""", Holder::class.java).at)
    }

    @Test
    fun localDate_parses() {
        assertEquals(LocalDate.of(2025, 7, 17), gson.fromJson("""{"day":"2025-07-17"}""", Holder::class.java).day)
    }

    @Test
    fun orderStatus_knownAndUnknown() {
        assertEquals(OrderStatus.SOLD, gson.fromJson("""{"status":"sold"}""", Holder::class.java).status)
        assertEquals(OrderStatus.UNKNOWN, gson.fromJson("""{"status":"shipped"}""", Holder::class.java).status)
        assertEquals(OrderStatus.UNKNOWN, OrderStatus.from(null))
    }

    @Test
    fun errorCode_knownAndUnknown() {
        assertEquals(ErrorCode.OUT_OF_STOCK, gson.fromJson("""{"code":"OUT_OF_STOCK"}""", Holder::class.java).code)
        assertEquals(ErrorCode.UNKNOWN, gson.fromJson("""{"code":"NEW_SERVER_CODE"}""", Holder::class.java).code)
        assertEquals(ErrorCode.UNKNOWN, ErrorCode.from(null))
    }

    @Test
    fun pageRequest_queryMapAndNext() {
        val req = PageRequest(sort = "name", order = "asc")
        assertEquals(mapOf("limit" to "20", "offset" to "0", "sort" to "name", "order" to "asc"), req.toQueryMap())
        assertEquals(20, req.next(Pagination(20, 0, 57))?.offset)
        assertEquals(40, req.next(Pagination(20, 20, 57))?.offset)
        assertNull(req.next(Pagination(20, 40, 57)))
        assertNull(req.next(null))
    }

    @Test
    fun loadMore_appendsUntilTotal() = runBlocking {
        val first = PageRequest(limit = 2)
        var calls = 0
        val r = loadMore(listOf(1, 2), first, Pagination(2, 0, 3)) { req ->
            calls++
            assertEquals(2, req.offset)
            ApiResult.Success(listOf(3), Pagination(2, 2, 3))
        }
        assertEquals(listOf(1, 2, 3), (r as ApiResult.Success).data)
        val done = loadMore(r.data, first.copy(offset = 2), r.pagination) { error("should not fetch") }
        assertEquals(listOf(1, 2, 3), (done as ApiResult.Success).data)
        assertEquals(1, calls)
    }
}
