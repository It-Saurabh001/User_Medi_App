package com.saurabh.mediuserapp.data.v1

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.ApiResult
import com.saurabh.mediuserapp.data.v1.core.PagedEnvelope
import com.saurabh.mediuserapp.data.v1.core.Pagination
import com.saurabh.mediuserapp.data.v1.core.safeApiCall
import com.saurabh.mediuserapp.data.v1.core.safeApiCallPaged
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class SafeApiCallTest {

    private val json = "application/json".toMediaType()

    private fun <T> err(code: Int, body: String): Response<ApiEnvelope<T>> =
        Response.error(code, body.toResponseBody(json))

    private fun apiError(result: ApiResult<*>): AppError.Api {
        assertTrue("expected Failure but was $result", result is ApiResult.Failure)
        val e = (result as ApiResult.Failure).error
        assertTrue("expected Api but was $e", e is AppError.Api)
        return e as AppError.Api
    }

    @Test
    fun success_unwrapsData() = runBlocking {
        val r = safeApiCall { Response.success(ApiEnvelope(200, "ok", "hello")) }
        assertEquals(ApiResult.Success("hello", null, "ok"), r)
    }

    @Test
    fun validationError_withFields() = runBlocking {
        val r = safeApiCall<String> {
            err(400, """{"status":400,"message":"Validation failed","code":"VALIDATION_ERROR","fields":{"email":"is required"}}""")
        }
        val e = apiError(r)
        assertEquals(400, e.httpStatus)
        assertEquals(ErrorCode.VALIDATION_ERROR, e.code)
        assertEquals("Validation failed", e.message)
        assertEquals(mapOf("email" to "is required"), e.fields)
    }

    @Test
    fun tokenExpired401() = runBlocking {
        val e = apiError(safeApiCall<String> {
            err(401, """{"status":401,"message":"Token has expired","code":"TOKEN_EXPIRED"}""")
        })
        assertEquals(401, e.httpStatus)
        assertEquals(ErrorCode.TOKEN_EXPIRED, e.code)
    }

    @Test
    fun outOfStock409_numericFieldStringified() = runBlocking {
        val e = apiError(safeApiCall<String> {
            err(409, """{"status":409,"message":"Insufficient stock","code":"OUT_OF_STOCK","fields":{"quantity":3}}""")
        })
        assertEquals(ErrorCode.OUT_OF_STOCK, e.code)
        assertEquals("3", e.fields?.get("quantity"))
        assertTrue(e.userMessage().contains("3"))
    }

    @Test
    fun unknownCode_mapsToUnknown() = runBlocking {
        val e = apiError(safeApiCall<String> {
            err(418, """{"status":418,"message":"teapot","code":"I_AM_A_TEAPOT"}""")
        })
        assertEquals(ErrorCode.UNKNOWN, e.code)
        assertEquals("teapot", e.message)
    }

    @Test
    fun emptyErrorBody_infersCodeFromHttp() = runBlocking {
        val e = apiError(safeApiCall<String> { err(404, "") })
        assertEquals(ErrorCode.NOT_FOUND, e.code)
        assertNull(e.fields)
    }

    @Test
    fun nonJsonErrorBody_infersCodeFromHttp() = runBlocking {
        val e = apiError(safeApiCall<String> { err(502, "<html>Bad gateway</html>") })
        assertEquals(ErrorCode.INTERNAL_ERROR, e.code)
    }

    @Test
    fun noContent204_successWithNull() = runBlocking {
        val r = safeApiCall<Any?> { Response.success<ApiEnvelope<Any?>>(204, null as ApiEnvelope<Any?>?) }
        assertTrue(r is ApiResult.Success)
        assertNull((r as ApiResult.Success).data)
    }

    @Test
    fun ioException_mapsToNetwork() = runBlocking {
        val r = safeApiCall<String> { throw IOException("offline") }
        assertEquals(ApiResult.Failure(AppError.Network), r)
    }

    @Test
    fun timeout_mapsToTimeout() = runBlocking {
        val r = safeApiCall<String> { throw SocketTimeoutException("timeout") }
        assertEquals(ApiResult.Failure(AppError.Timeout), r)
    }

    @Test
    fun paged_successCarriesPagination() = runBlocking {
        val p = Pagination(20, 0, 57)
        val r = safeApiCallPaged { Response.success(PagedEnvelope(200, "ok", listOf(1, 2), p, null)) }
        assertEquals(ApiResult.Success(listOf(1, 2), p, "ok"), r)
    }

    @Test
    fun paged_nullDataIsEmptyList() = runBlocking {
        val r = safeApiCallPaged<Int> { Response.success(PagedEnvelope(200, "ok", null, null, null)) }
        assertEquals(emptyList<Int>(), (r as ApiResult.Success).data)
    }
}
