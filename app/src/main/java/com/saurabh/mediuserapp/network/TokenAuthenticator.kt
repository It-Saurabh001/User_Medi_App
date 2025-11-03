package com.saurabh.mediuserapp.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator1(
    private val tokenManager: TokenManager,
    private val apiService: ApiServices
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If response is 401 (Unauthorized), try to refresh token

        // Avoid infinite loop - if already tried with new token, don't retry
        if (response.request.header("Authorization")?.contains("Bearer") == true
            && responseCount(response) >= 3) {
            return null // Give up after 3 attempts
        }

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // Synchronously refresh token (blocking call)
        val newAccessToken = synchronized(this) {
            try {
                runBlocking {
                    refreshAccessToken(refreshToken)
                }
            } catch (e: Exception) {
                null
            }
        }

        // If refresh failed, return null (will trigger logout)
        if (newAccessToken == null) {
            tokenManager.clearTokens()
            return null
        }

        // Update token in manager
        tokenManager.updateAccessToken(newAccessToken)

        // Retry original request with new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val response = apiService.refreshToken("Bearer $refreshToken")
            if (response.isSuccessful) {
                response.body()?.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Count how many times we've tried to authenticate
    private fun responseCount(response: Response): Int {
        var result = 1
        var currentResponse = response.priorResponse
        while (currentResponse != null) {
            result++
            currentResponse = currentResponse.priorResponse
        }
        return result
    }
}


class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val tempApiService: ApiServices
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If response is 401 and we have a refresh token, try to refresh
        if (response.code == 401) {
            Log.d("TokenAuthenticator", "Received 401, attempting token refresh")

            val refreshToken = tokenManager.getRefreshToken()

            if (refreshToken.isNullOrEmpty()) {
                Log.e("TokenAuthenticator", "No refresh token available")
                tokenManager.clearTokens()
                return null
            }

            // Prevent infinite loop - if this is already a retry, don't retry again
            if (response.request.header("Authorization")?.let {
                    it == "Bearer ${tokenManager.getAccessToken()}"
                } == true && responseCount(response) >= 2) {
                Log.e("TokenAuthenticator", "Already retried, clearing tokens")
                tokenManager.clearTokens()
                return null
            }

            // Try to refresh token synchronously
            val newAccessToken = refreshAccessToken(refreshToken)

            return if (newAccessToken != null) {
                Log.d("TokenAuthenticator", "Token refreshed, retrying request")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Log.e("TokenAuthenticator", "Token refresh failed")
                tokenManager.clearTokens()
                null
            }
        }

        return null
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            Log.d("TokenAuthenticator", "Calling refresh token API")

            val response = runBlocking {
                tempApiService.refreshToken("Bearer $refreshToken")
            }

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                tokenManager.updateAccessToken(newAccessToken)
                Log.d("TokenAuthenticator", "Access token updated successfully")
                newAccessToken
            } else {
                Log.e("TokenAuthenticator", "Refresh token API failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Exception during token refresh: ${e.message}")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
