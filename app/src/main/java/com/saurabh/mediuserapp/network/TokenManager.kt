package com.saurabh.mediuserapp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Tokens issued by verify-otp / refresh (v1) or the legacy refresh endpoint. */
data class TokenBundle(
    val accessToken: String,
    val refreshToken: String,
    val role: String? = null,
    val accountId: String? = null,
    val accountType: String? = null,
    val expiresInSeconds: Long? = null
)

/**
 * Single token store for old and v1 APIs. Backed by EncryptedSharedPreferences
 * (one-time migration from the old plain "auth_prefs").
 */
class TokenManager internal constructor(
    private val prefs: SharedPreferences,
    private val clock: () -> Long = System::currentTimeMillis
) {

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()

    /** The ONE lock guarding token refresh (old and v1 paths). */
    val refreshLock = ReentrantLock()

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "auth_prefs"
        private const val SECURE_PREFS_NAME = "auth_prefs_secure"
        private const val KEY_MIGRATED = "migrated_from_plain"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_DEVICE_ID = "device_id"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(createPrefs(context.applicationContext)).also {
                    instance = it
                }
            }
        }

        private fun createPrefs(context: Context): SharedPreferences {
            val legacy = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val secure = try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.e(TAG, "EncryptedSharedPreferences unavailable, using plain prefs", e)
                return legacy
            }
            if (!secure.getBoolean(KEY_MIGRATED, false)) {
                val editor = secure.edit()
                legacy.all.forEach { (key, value) ->
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Long -> editor.putLong(key, value)
                        is Int -> editor.putInt(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Float -> editor.putFloat(key, value)
                    }
                }
                if (editor.putBoolean(KEY_MIGRATED, true).commit()) {
                    legacy.edit().clear().commit()
                }
            }
            return secure
        }
    }

    // Save tokens after successful login/OTP verification
    fun saveTokens(accessToken: String, refreshToken: String, role: String, userId: String) {
        Log.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Saving tokens for user: $userId (Role: $role)")
        val committed = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .commit()

        if (!committed) {
            Log.e(TAG, " TokenManager=> saveTokens: SharedPreferences commit() returned false")
        }
    }

    fun updateTokens(newAccessToken: String, newRefreshToken: String?, role: String? = null) {
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
            Log.e(TAG, "TokenManager=> updateTokens: SharedPreferences commit() returned false")
        }
    }

    /** v1: persist verify-otp / refresh result (both tokens, role, account id/type, expiry). */
    fun saveSession(bundle: TokenBundle) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, bundle.accessToken)
            .putString(KEY_REFRESH_TOKEN, bundle.refreshToken)
        bundle.role?.let { editor.putString(KEY_USER_ROLE, it) }
        bundle.accountId?.let {
            editor.putString(KEY_ACCOUNT_ID, it)
            editor.putString(KEY_USER_ID, it) // old getUserId() keeps working
        }
        bundle.accountType?.let { editor.putString(KEY_ACCOUNT_TYPE, it) }
        bundle.expiresInSeconds?.let { editor.putLong(KEY_EXPIRES_AT, clock() + it * 1000) }
        if (!editor.commit()) {
            Log.e(TAG, "TokenManager=> saveSession: SharedPreferences commit() returned false")
        }
    }

    /**
     * Single-flight refresh used by BOTH old and v1 authenticator paths.
     * If another thread already rotated the access token (stored != [requestAccessToken]) it is reused.
     * Otherwise [refresher] is called once with the stored refresh token; its result is saved.
     * Returns the access token to retry with, or null on failure (caller clears the session).
     */
    fun refreshSingleFlight(
        requestAccessToken: String?,
        refresher: (refreshToken: String) -> TokenBundle?
    ): String? = refreshLock.withLock {
        val stored = getAccessToken()
        if (!stored.isNullOrEmpty() && stored != requestAccessToken) return@withLock stored
        val refreshToken = getRefreshToken()
        if (refreshToken.isNullOrEmpty()) return@withLock null
        val bundle = refresher(refreshToken) ?: return@withLock null
        if (bundle.accessToken.isBlank() || bundle.refreshToken.isBlank()) return@withLock null
        saveSession(bundle)
        bundle.accessToken
    }

    // Get access token
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    // Get refresh token
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // Get user role
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    /** v1 name for [getUserRole]. */
    fun getRole(): String? = getUserRole()

    // Get user ID
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /** v1 account_id (falls back to the old user_id). */
    fun getAccountId(): String? = prefs.getString(KEY_ACCOUNT_ID, null) ?: getUserId()

    fun getAccountType(): String? = prefs.getString(KEY_ACCOUNT_TYPE, null)

    /** Epoch millis when the access token expires (v1 only), null if unknown. */
    fun getExpiresAt(): Long? = if (prefs.contains(KEY_EXPIRES_AT)) prefs.getLong(KEY_EXPIRES_AT, 0L) else null

    fun isAccessTokenExpired(): Boolean = getExpiresAt()?.let { clock() >= it } ?: false

    /** Device id returned by POST devices (cleared with the session). */
    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)

    fun saveDeviceId(deviceId: String?) {
        prefs.edit { if (deviceId == null) remove(KEY_DEVICE_ID) else putString(KEY_DEVICE_ID, deviceId) }
    }

    fun saveUserId(userId: String) {
        prefs.edit { putString(KEY_USER_ID, userId) }
    }

    fun saveRole(role: String) {
        prefs.edit { putString(KEY_USER_ROLE, role) }
    }

    // Update only access token (after refresh)
    fun updateAccessToken(newAccessToken: String) {
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Updating access token")
        prefs.edit { putString(KEY_ACCESS_TOKEN, newAccessToken) }
    }

    // Clear all tokens (logout)
    fun clearTokens() {
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Clearing all tokens")
        prefs.edit { clear() }
    }

    /** Synchronous clear without emitting an event. */
    fun clear(): Boolean = prefs.edit().clear().commit()

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        val loggedIn = !getAccessToken().isNullOrEmpty()
        com.saurabh.mediuserapp.utils.AppLogger.d(com.saurabh.mediuserapp.utils.AppLogger.TAG_AUTH, "Checking login status: $loggedIn")
        return loggedIn
    }

    fun invalidateSession() {
        val isCleared = prefs.edit().clear().commit()
        if (isCleared) {
            val emitted = _sessionExpiredEvent.tryEmit(Unit)
            Log.w(TAG, "TokenManager=> invalidateSession: session cleared, event emitted=$emitted")
        }
    }
}
