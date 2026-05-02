package com.saurabh.mediuserapp.network

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
            "/admin/resetAdminPasswordWithOtp"
        )
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        // Check if current URL is excluded
        val isExcluded = EXCLUDED_PATHS.any{path.startsWith(it)}
        // If excluded or no token, send request as is
        val token = tokenManager.getAccessToken()
        val hasValidToken = !token.isNullOrBlank()
        println("Has valid token: $hasValidToken, Is excluded: $isExcluded")
        // If excluded or no valid token, proceed without aut

        if (isExcluded || !hasValidToken) {
            return chain.proceed(originalRequest)
        }

        // Add token to request
        println("Adding brarer token ")

        // Add JWT token to Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${token}")
            .build()

        val response = chain.proceed(authenticatedRequest)
        println("Response code: ${response.code} for $path")
        if (response.code == 401) {
            println("Received 401 for $path, token might be invalid or expired")
        }
        return response
    }




}