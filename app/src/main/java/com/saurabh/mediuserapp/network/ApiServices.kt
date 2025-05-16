package com.saurabh.mediuserapp.network

import com.saurabh.mediuserapp.network.response.CreateUserResponse
import com.saurabh.mediuserapp.network.response.GetSpecificUserResponse
import com.saurabh.mediuserapp.network.response.LoginUserResponse
import com.saurabh.mediuserapp.utils.User
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServices {
    // need to define end points


    // for signup use post
    @FormUrlEncoded
    @POST("createUser")
    suspend fun createUser(

            // for table query send krte the
        // but here form is in use
        // so we need to call field
        @Field("name") name : String,
        @Field("password") password : String,
        @Field("phoneNumber") phoneNumber : String,
        @Field("email") email : String,
        @Field("pincode") pinCode : String,
        @Field("address") address : String

    ): Response<CreateUserResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun loginUser(
        @Field("email") email : String,
        @Field("password") password : String
    ): Response<LoginUserResponse>

    @FormUrlEncoded
    @POST("getSpecificUser")
    suspend fun getSpecificUser(
        @Field("user_id") userId : String
    ): Response<GetSpecificUserResponse>



    @GET("getAllUsers")
    suspend fun getAllUsers(): Response<List<User>>
}



