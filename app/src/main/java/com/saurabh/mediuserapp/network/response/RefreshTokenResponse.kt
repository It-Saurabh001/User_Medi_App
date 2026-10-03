package com.saurabh.mediuserapp.network.response

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    val role: String? = null,
    val status: Int? = null,
    val message: String? = null
)
