package com.saurabh.mediuserapp.data.v1.api

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface UsersApi {
    /** Public signup (201). Fields via UserCreateRequest.toParts(); [image] optional. */
    @Multipart
    @POST("users")
    suspend fun createUser(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<UserDto>>

    @GET("users/me")
    suspend fun getMe(): Response<ApiEnvelope<UserDto>>

    /** Fields via ProfileUpdateRequest.toParts(); [image] optional. */
    @Multipart
    @PATCH("users/me")
    suspend fun updateMe(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<UserDto>>
}
