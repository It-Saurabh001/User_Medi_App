package com.saurabh.mediuserapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        val EXCLUDED_PATHS = listOf(
            "/user/login",
            "/user/create",
            "/admin/login",
            "/admin/create",
            "/user/verifyUserOtp",
            "/admin/verifyOtp",
            "/user/requestUserPasswordReset",
            "/user/resetUserPasswordWithOtp",
            "/admin/requestAdminPasswordReset",
            "/admin/resetAdminPasswordWithOtp",
            "/admin/refreshToken"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        if (V1Paths.isV1Path(path) || V1Paths.isHealth(path)) return interceptV1(chain)

        // Check if current URL is excluded
        val isExcluded = EXCLUDED_PATHS.any { path.startsWith(it) }
        // If excluded or no token, send request as is
        val token = tokenManager.getAccessToken()
        val hasValidToken = !token.isNullOrBlank()
        println("Has valid token: $hasValidToken, Is excluded: $isExcluded")

        if (isExcluded || !hasValidToken) {
            return chain.proceed(originalRequest)
        }

        println("Adding brarer token ")

        // Add JWT token to Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${token}")
            .build()

        val response = chain.proceed(authenticatedRequest)
        println("Response code: ${response.code} for $path")
        Log.d("TAG", "Response code: ${response.code} for $path")
        if (response.code == 401) {
            println("Received 401 for $path, token might be invalid or expired")
        }
        if (response.code == 422) {
            Log.w("AuthInterceptor", "Received 422 for $path: corrupt or invalid JWT. Invalidating session.")
            tokenManager.invalidateSession()
        }
        return response
    }

    /**
     * v1: Bearer access token except public paths; auth/refresh sends the REFRESH token.
     * An Authorization header already set by the caller is never overwritten.
     */
    private fun interceptV1(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null || V1Paths.isPublic(request)) {
            return chain.proceed(request)
        }
        val token = if (V1Paths.isRefresh(request)) tokenManager.getRefreshToken() else tokenManager.getAccessToken()
        if (token.isNullOrBlank()) return chain.proceed(request)
        return chain.proceed(
            request.newBuilder().header("Authorization", "Bearer $token").build()
        )
    }
}
