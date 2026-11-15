package com.saurabh.mediuserapp.network

import com.saurabh.mediuserapp.data.v1.core.AppEvent
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import com.saurabh.mediuserapp.data.v1.error.ErrorBody
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * v1 only: 403 ACCOUNT_BLOCKED -> AccountBlocked, 401 TOKEN_INVALID -> SessionInvalid,
 * 429 -> RateLimited. Peeks the body (never consumes it).
 */
class ErrorEventInterceptor @Inject constructor(
    private val appEvents: AppEvents
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (!V1Paths.isV1(request)) return response
        when (response.code) {
            429 -> appEvents.emit(AppEvent.RateLimited)
            401, 403 -> {
                val code = ErrorCode.from(
                    ErrorBody.parse(runCatching { response.peekBody(64L * 1024).string() }.getOrNull())?.code
                )
                if (response.code == 403 && code == ErrorCode.ACCOUNT_BLOCKED) appEvents.emit(AppEvent.AccountBlocked)
                if (response.code == 401 && code == ErrorCode.TOKEN_INVALID) appEvents.emit(AppEvent.SessionInvalid)
            }
        }
        return response
    }
}
