package com.saurabh.mediuserapp.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // URLs jahan JWT token nahi chahiye (login, signup, etc.)
        val excludedPaths = listOf(
            "/user/login",
            "/user/create",
            "/admin/login",
            "/admin/create",
            "/user/verifyUserOtp",
            "/admin/verifyOtp",
            "/user/requestUserPasswordReset",
            "/user/resetUserPasswordWithOtp",
            "/admin/requestAdminPasswordReset",
            "/admin/resetAdminPasswordWithOtp"
        )

        // Check if current URL is excluded
        val isExcluded = excludedPaths.any { originalRequest.url.encodedPath.contains(it) }

        // If excluded or no token, send request as is
        if (isExcluded || tokenManager.getAccessToken() == null) {
            return chain.proceed(originalRequest)
        }

        // Add JWT token to Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${tokenManager.getAccessToken()}")
            .build()

        return chain.proceed(authenticatedRequest)
    }




}