package com.saurabh.mediuserapp.data.v1.api

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.dto.GoogleLoginRequest
import com.saurabh.mediuserapp.data.v1.dto.LoginData
import com.saurabh.mediuserapp.data.v1.dto.LoginRequest
import com.saurabh.mediuserapp.data.v1.dto.LogoutData
import com.saurabh.mediuserapp.data.v1.dto.LogoutRequest
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetConfirmRequest
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetRequest
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetRequestData
import com.saurabh.mediuserapp.data.v1.dto.RefreshData
import com.saurabh.mediuserapp.data.v1.dto.TokenData
import com.saurabh.mediuserapp.data.v1.dto.VerifyOtpRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** v1 auth endpoints (relative to `.../api/v1/`). Built on @V1AuthRetrofit (no authenticator). */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<ApiEnvelope<TokenData>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<ApiEnvelope<TokenData>>

    /** Blocking; used only by TokenAuthenticator. Bearer = REFRESH token. */
    @POST("auth/refresh")
    fun refreshCall(@Header("Authorization") bearerRefreshToken: String): Call<ApiEnvelope<RefreshData>>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<ApiEnvelope<LogoutData>>

    @POST("auth/password-reset/request")
    suspend fun requestPasswordReset(@Body body: PasswordResetRequest): Response<ApiEnvelope<PasswordResetRequestData>>

    /** data is null on success. */
    @POST("auth/password-reset/confirm")
    suspend fun confirmPasswordReset(@Body body: PasswordResetConfirmRequest): Response<ApiEnvelope<Any?>>
}
