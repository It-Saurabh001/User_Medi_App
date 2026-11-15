package com.saurabh.mediuserapp.network

import com.saurabh.mediuserapp.BuildConfig
import com.saurabh.mediuserapp.data.v1.core.AppEvent
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** v1 only: sends X-App-Version; 426 -> AppEvent.UpgradeRequired(X-Min-App-Version). */
class VersionInterceptor @Inject constructor(
    private val appEvents: AppEvents
) : Interceptor {

    companion object {
        const val HEADER_APP_VERSION = "X-App-Version"
        const val HEADER_MIN_VERSION = "X-Min-App-Version"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!V1Paths.isV1(request)) return chain.proceed(request)
        val response = chain.proceed(
            request.newBuilder().header(HEADER_APP_VERSION, BuildConfig.VERSION_NAME).build()
        )
        if (response.code == 426) {
            appEvents.emit(AppEvent.UpgradeRequired(response.header(HEADER_MIN_VERSION)))
        }
        return response
    }
}
