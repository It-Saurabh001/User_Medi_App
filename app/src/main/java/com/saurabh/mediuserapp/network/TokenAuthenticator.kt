package com.saurabh.mediuserapp.network

import android.util.Log
import com.saurabh.mediuserapp.data.v1.api.AuthApi
import com.saurabh.mediuserapp.data.v1.core.AppEvent
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import com.saurabh.mediuserapp.data.v1.error.ErrorBody
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Lazy<ApiServices>,
    private val v1AuthApi: Lazy<AuthApi>,
    private val appEvents: AppEvents
) : Authenticator {

    companion object {
        private const val TAG = "TAG"
        private const val MAX_ATTEMPTS = 2
        private const val PEEK_BYTES = 64L * 1024
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "authenticate() triggered, response code: ${response.code}")
        if (response.code != 401) return null
        return if (V1Paths.isV1(response.request)) authenticateV1(response) else authenticateLegacy(response)
    }

    // ---------------- v1 ----------------

    private fun authenticateV1(response: Response): Request? {
        val request = response.request
        // Public calls (login etc.) and the refresh call itself never trigger a refresh.
        if (V1Paths.isPublic(request) || V1Paths.isRefresh(request)) return null
        val requestToken = request.bearerToken() ?: return null

        val code = ErrorCode.from(
            ErrorBody.parse(runCatching { response.peekBody(PEEK_BYTES).string() }.getOrNull())?.code
        )
        if (code != ErrorCode.TOKEN_EXPIRED || responseCount(response) >= MAX_ATTEMPTS) {
            Log.w(TAG, "Authenticator(v1)=> 401 $code, giving up")
            endSession()
            return null
        }

        val newToken = tokenManager.refreshSingleFlight(requestToken) { refreshToken -> refreshV1(refreshToken) }
        if (newToken == null) {
            Log.e(TAG, "Authenticator(v1)=> refresh failed — invalidating session")
            endSession()
            return null
        }
        return request.newBuilder().header("Authorization", "Bearer $newToken").build()
    }

    private fun refreshV1(refreshToken: String): TokenBundle? = try {
        val res = v1AuthApi.get().refreshCall("Bearer $refreshToken").execute()
        val data = res.body()?.data
        if (res.isSuccessful && data != null) {
            TokenBundle(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                role = data.role,
                accountId = data.accountId,
                accountType = data.accountType,
                expiresInSeconds = data.expiresIn
            )
        } else {
            Log.e(TAG, "Authenticator(v1)=> refresh HTTP ${res.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Authenticator(v1)=> refresh exception: ${e.message}", e)
        null
    }

    private fun endSession() {
        tokenManager.invalidateSession()
        appEvents.emit(AppEvent.SessionInvalid)
    }

    // ---------------- legacy (unchanged behaviour) ----------------

    private fun authenticateLegacy(response: Response): Request? {
        if (responseCount(response) >= MAX_ATTEMPTS) {
            Log.e(TAG, "Authenticator=>Already retried once — aborting, clearing session $response")
            tokenManager.invalidateSession()
            return null
        }
        if (tokenManager.getRefreshToken().isNullOrEmpty()) {
            Log.e(TAG, "Authenticator=>No refresh token available — clearing session")
            tokenManager.invalidateSession()
            return null
        }
        val newToken = tokenManager.refreshSingleFlight(response.request.bearerToken()) { refreshLegacy(it) }
        return if (newToken != null) {
            response.request.newBuilder().header("Authorization", "Bearer $newToken").build()
        } else {
            Log.e(TAG, "Authenticator=>Token refresh failed — invalidating session")
            tokenManager.invalidateSession()
            null
        }
    }

    private fun refreshLegacy(refreshToken: String): TokenBundle? = try {
        val res = apiServiceProvider.get().refreshToken("Bearer $refreshToken").execute()
        val body = res.body()
        val access = body?.accessToken
        val refresh = body?.refreshToken
        if (res.isSuccessful && !access.isNullOrBlank() && !refresh.isNullOrBlank()) {
            TokenBundle(accessToken = access, refreshToken = refresh, role = body.role)
        } else {
            Log.e(TAG, "Authenticator=>Refresh endpoint returned HTTP ${res.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception calling refresh endpoint: ${e.message}", e)
        null
    }

    private fun Request.bearerToken(): String? =
        header("Authorization")?.removePrefix("Bearer ")?.trim()?.takeIf { it.isNotEmpty() }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
