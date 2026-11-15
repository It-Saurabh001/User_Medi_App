package com.saurabh.mediuserapp.repo

import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.data.v1.api.AuthApi
import com.saurabh.mediuserapp.data.v1.api.DevicesApi
import com.saurabh.mediuserapp.data.v1.api.HealthApi
import com.saurabh.mediuserapp.data.v1.api.MetaApi
import com.saurabh.mediuserapp.data.v1.api.OrdersApi
import com.saurabh.mediuserapp.data.v1.api.ProductsApi
import com.saurabh.mediuserapp.data.v1.api.UsersApi
import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.ApiResult
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.dto.OrderCreateRequest
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.network.TokenAuthenticatorTest
import com.saurabh.mediuserapp.network.TokenManager
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.time.Instant
import java.time.LocalDate

class RepositoryTest {

    /** Fake API: suspend functions return their value directly through the proxy. */
    @Suppress("UNCHECKED_CAST")
    private fun <T> fake(cls: Class<T>, handler: (Method, Array<Any?>) -> Any? = { m, _ -> error("unexpected ${m.name}") }): T =
        Proxy.newProxyInstance(cls.classLoader, arrayOf(cls)) { _, m, args -> handler(m, args ?: emptyArray()) } as T

    private fun repo(
        tokens: TokenManager,
        auth: AuthApi = fake(AuthApi::class.java),
        devices: DevicesApi = fake(DevicesApi::class.java),
        orders: OrdersApi = fake(OrdersApi::class.java)
    ) = Repository(
        auth, fake(UsersApi::class.java), fake(ProductsApi::class.java), orders, devices,
        fake(MetaApi::class.java), fake(HealthApi::class.java), tokens
    )

    private fun tokens() = TokenManager(TokenAuthenticatorTest.FakePrefs()).also {
        it.saveTokens("access", "refresh", "user", "UID_1")
        it.saveDeviceId("DEV_1")
    }

    @Test
    fun queryOf_dropsBlankAndConvertsTypes() {
        val q = queryOf(
            "a" to null, "b" to "  ", "c" to true, "d" to LocalDate.of(2026, 1, 2),
            "e" to Instant.parse("2026-01-02T03:04:05Z"), "f" to OrderStatus.SOLD, "g" to "x"
        )
        assertEquals(
            mapOf("c" to "true", "d" to "2026-01-02", "e" to "2026-01-02T03:04:05Z", "f" to "sold", "g" to "x"), q
        )
    }

    @Test
    fun filters_useApiQueryNames() {
        assertEquals(
            mapOf("q" to "para", "schedule_type" to "OTC", "requires_prescription" to "false", "in_stock" to "true"),
            ProductFilter(q = "para", scheduleType = "OTC", requiresPrescription = false, inStock = true).toQuery()
        )
        assertEquals(
            mapOf("status" to "pending", "from" to "2026-01-01", "to" to "2026-02-01"),
            OrderFilter(OrderStatus.PENDING, DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1))).toQuery()
        )
    }

    @Test
    fun v1Flow_failureBecomesErrorWithAppErrorIntact() = runBlocking {
        val err = AppError.Api(409, ErrorCode.OUT_OF_STOCK, "Only 5", mapOf("quantity" to "available: 5"))
        val states = v1Flow<String> { ApiResult.Failure(err) }.toList()
        assertTrue(states[0] is ResultState.Loading)
        val e = (states[1] as ResultState.Error).exception as AppException
        assertEquals(err, e.error)
    }

    @Test
    fun logout_clearsTokensEvenWhenApisFail() = runBlocking {
        val t = tokens()
        val auth = fake(AuthApi::class.java) { _, _ -> throw IOException("offline") }
        val devices = fake(DevicesApi::class.java) { _, _ -> throw IOException("offline") }
        val states = repo(t, auth, devices).logout().toList()
        assertTrue(states.last() is ResultState.Success)
        assertNull(t.getAccessToken())
        assertNull(t.getRefreshToken())
        assertNull(t.getDeviceId())
    }

    @Test
    fun logout_serverErrorStillClears() = runBlocking {
        val t = tokens()
        val auth = fake(AuthApi::class.java) { _, _ ->
            Response.error<ApiEnvelope<Any>>(500, "{}".toResponseBody("application/json".toMediaType()))
        }
        val devices = fake(DevicesApi::class.java) { _, _ ->
            Response.error<ApiEnvelope<Any>>(404, "{}".toResponseBody("application/json".toMediaType()))
        }
        repo(t, auth, devices).logout().toList()
        assertNull(t.getAccessToken())
    }

    @Test
    fun createOrder_forwardsGivenIdempotencyKey() = runBlocking {
        var seenKey: Any? = "unset"
        val orders = fake(OrdersApi::class.java) { m, args ->
            assertEquals("createOrder", m.name)
            seenKey = args[2]
            Response.error<ApiEnvelope<Any>>(400, "{}".toResponseBody("application/json".toMediaType()))
        }
        repo(tokens(), orders = orders)
            .createOrder(OrderCreateRequest("PROD_1", 2), null, "key-123").toList()
        assertEquals("key-123", seenKey)
    }
}
