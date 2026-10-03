package com.saurabh.mediuserapp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TokenManager  private constructor(context: Context){

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()


    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // Save tokens after successful login/OTP verification
    fun saveTokens(accessToken: String, refreshToken: String, role: String, userId: String) {
        Log.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Saving tokens for user: $userId (Role: $role)")
        val committed =prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .commit()

        if (!committed) {
            Log.e("TokenManager", " TokenManager=> saveTokens: SharedPreferences commit() returned false")
        }
    }

    fun updateTokens(newAccessToken: String, newRefreshToken: String?,role: String? = null) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)

        if (!newRefreshToken.isNullOrBlank()) {
            editor.putString(KEY_REFRESH_TOKEN, newRefreshToken)
        }
        if (role != null) {
            editor.putString(KEY_USER_ROLE, role)
        }

        val committed = editor.commit() // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", "TokenManager=> updateTokens: SharedPreferences commit() returned false")
        }
    }




    // Get access token
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Get refresh token
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // Get user role
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    // Get user ID
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    fun saveUserId(userId: String) {
        return prefs.edit { putString(KEY_USER_ID, userId).apply() }

    }
    fun saveRole(role: String) {
        return prefs.edit { putString(KEY_USER_ROLE, role).apply() }
    }

    // Update only access token (after refresh)
    fun updateAccessToken(newAccessToken: String) {
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Updating access token")
        prefs.edit { putString(KEY_ACCESS_TOKEN, newAccessToken) }
    }

    // Clear all tokens (logout)
    fun clearTokens() {
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Clearing all tokens")
        prefs.edit { clear().apply() }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        val loggedIn = !getAccessToken().isNullOrEmpty()
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Checking login status: $loggedIn")
        return loggedIn
    }

    fun invalidateSession() {
        val editor = prefs.edit()
        editor.clear()

        val isCleared = editor.commit()
        if (isCleared) {
            // UI ko batao ki session perfectly out ho gaya hai
            val emitted = _sessionExpiredEvent.tryEmit(Unit)
            Log.w("TokenManager", "TokenManager=> invalidateSession Ab signinscreen pr jaana hai : session cleared, event emitted=$emitted")

        }
    }





}