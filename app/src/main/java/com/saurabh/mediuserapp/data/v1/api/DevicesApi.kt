package com.saurabh.mediuserapp.data.v1.api

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.dto.DeviceDto
import com.saurabh.mediuserapp.data.v1.dto.DeviceRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface DevicesApi {
    /** 201 for a new token, 200 for an existing one. */
    @POST("devices")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): Response<ApiEnvelope<DeviceDto>>

    @DELETE("devices/{device_id}")
    suspend fun unregisterDevice(@Path("device_id") deviceId: String): Response<ApiEnvelope<DeviceDto>>
}
