package com.saurabh.mediuserapp.network

import android.content.SharedPreferences
import com.saurabh.mediuserapp.data.v1.api.AuthApi
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import com.saurabh.mediuserapp.data.v1.core.V1Gson
import dagger.Lazy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenManager: TokenManager
    private lateinit var authenticator: TokenAuthenticator
    private val refreshCalls = AtomicInteger(0)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.path?.startsWith("/api/v1/auth/refresh") == true &&
                    request.getHeader("Authorization") == "Bearer refresh-1"
                ) {
                    refreshCalls.incrementAndGet()
                    Thread.sleep(200) // widen the race window
                    return MockResponse().setResponseCode(200).setBody(
                        """{"status":200,"message":"Token refreshed","data":{"access_token":"access-2",
                        "refresh_token":"refresh-2","token_type":"Bearer","expires_in":3600,"role":"user",
                        "account_type":"user","account_id":"UID_1"}}"""
                    )
                }
                return MockResponse().setResponseCode(401)
                    .setBody("""{"status":401,"message":"Token has been revoked","code":"TOKEN_INVALID"}""")
            }
        }
        server.start()

        tokenManager = TokenManager(FakePrefs())
        tokenManager.saveTokens("access-1", "refresh-1", "user", "UID_1")

        val authApi = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
            .build()
            .create(AuthApi::class.java)

        authenticator = TokenAuthenticator(
            tokenManager,
            Lazy { error("legacy API unused") },
            Lazy { authApi },
            AppEvents()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun unauthorized(code: String, token: String = "access-1"): Response {
        val request = Request.Builder()
            .url(server.url("/api/v1/orders"))
            .header("Authorization", "Bearer $token")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("""{"status":401,"message":"x","code":"$code"}""".toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun parallelTokenExpired_refreshesExactlyOnce() {
        val threads = 5
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val results = (1..threads).map {
            pool.submit<Request?> {
                start.await()
                authenticator.authenticate(null, unauthorized("TOKEN_EXPIRED"))
            }
        }
        start.countDown()
        val retried = results.map { it.get(10, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, refreshCalls.get())
        retried.forEach { assertEquals("Bearer access-2", it?.header("Authorization")) }
        assertEquals("access-2", tokenManager.getAccessToken())
        assertEquals("refresh-2", tokenManager.getRefreshToken())
        assertEquals("user", tokenManager.getRole())
        assertEquals("UID_1", tokenManager.getAccountId())
        assertEquals("user", tokenManager.getAccountType())
    }

    @Test
    fun tokenInvalid_clearsSessionWithoutRefresh() {
        assertNull(authenticator.authenticate(null, unauthorized("TOKEN_INVALID")))
        assertEquals(0, refreshCalls.get())
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun refreshRejected_clearsSession() {
        tokenManager.saveTokens("access-1", "revoked-refresh", "user", "UID_1")
        assertNull(authenticator.authenticate(null, unauthorized("TOKEN_EXPIRED")))
        assertNull(tokenManager.getRefreshToken())
    }

    /** In-memory SharedPreferences for JVM tests. */
    internal class FakePrefs : SharedPreferences {
        private val map = java.util.concurrent.ConcurrentHashMap<String, Any>()

        override fun getAll(): MutableMap<String, *> = HashMap(map)
        override fun getString(key: String, defValue: String?): String? = map[key] as String? ?: defValue
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String, defValue: Int): Int = map[key] as Int? ?: defValue
        override fun getLong(key: String, defValue: Long): Long = map[key] as Long? ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = map[key] as Float? ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as Boolean? ?: defValue
        override fun contains(key: String): Boolean = map.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val pending = HashMap<String, Any?>()
            private var clear = false
            override fun putString(key: String, value: String?) = apply { pending[key] = value }
            override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values }
            override fun putInt(key: String, value: Int) = apply { pending[key] = value }
            override fun putLong(key: String, value: Long) = apply { pending[key] = value }
            override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
            override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
            override fun remove(key: String) = apply { pending[key] = null }
            override fun clear() = apply { clear = true }
            override fun commit(): Boolean {
                synchronized(map) {
                    if (clear) map.clear()
                    pending.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
                }
                return true
            }
            override fun apply() {
                commit()
            }
        }
    }
}
