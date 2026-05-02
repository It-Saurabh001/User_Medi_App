package com.saurabh.mediuserapp.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val tempApiService: ApiServices
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        // 1. Agar error 401 nahi hai, to hume kuch nahi karna
        if (response.code != 401) {
            return null
        }
        // If response is 401 and we have a refresh token, try to refresh
        Log.d("TokenAuthenticator", "Received 401, attempting token refresh")
//        Refresh Token nikalo (TokenManager se)
        val refreshToken = tokenManager.getRefreshToken()
        // Agar Refresh Token hi nahi hai, to User ko seedha Logout karo
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenAuthenticator", "No refresh token available")
            tokenManager.clearTokens()
            return null
        }

//      SYNCHRONIZED BLOCK
        // Agar ek saath 4-5 API fail hoti hain, to ye block rokega ki refresh API baar-baar call na ho
        synchronized(this) {
            // Check karo: Kya kisi dusri thread ne token already update kar diya hai?
            val newAccessToken = tokenManager.getAccessToken()
            val requestAccessToken =
                response.request.header("Authorization")?.replace("Bearer ", "")

            // Agar TokenManager wala token aur Request wala token alag hai,
            // iska matlab token already refresh ho chuka hai. Bas retry karo.
            if (newAccessToken != null && newAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }

//                Retry Count Check (Infinite Loop se bachne ke liye)
            if (responseCount(response) >= 2) {
                tokenManager.clearTokens() // Retry limit over, logout
                Log.e("TokenAuthenticator", "Already retried, clearing tokens")

                return null
            }

//                API Call to Refresh Token
            // runBlocking zaroori hai kyunki ye method background thread par hai par result sync chahiye
            val refreshedToken = runBlocking {
                refreshAccessToken(refreshToken)
            }

//                Result Handling
            return if (refreshedToken != null) {
                // Success: Sirf Access Token update karo (TokenManager ka method use kiya)
                tokenManager.updateAccessToken(refreshedToken)
                Log.d("TokenAuthenticator", "Token refreshed, retrying request")

                // Retry Original Request with NEW Token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()
            } else {
                // Failure: Refresh Token bhi invalid/expire ho gaya
                tokenManager.clearTokens() // Logout
                Log.e("TokenAuthenticator", "Token refresh failed")

                null
            }

        }

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