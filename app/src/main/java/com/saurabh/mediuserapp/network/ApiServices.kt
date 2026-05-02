package com.saurabh.mediuserapp.network

import com.saurabh.mediuserapp.network.response.CreateOrderResponse
import com.saurabh.mediuserapp.network.response.CreateUserResponse
import com.saurabh.mediuserapp.network.response.GetAllProductResponse
import com.saurabh.mediuserapp.network.response.GetOrderByIdResponse
import com.saurabh.mediuserapp.network.response.GetSpecificProductResponse
import com.saurabh.mediuserapp.network.response.GetSpecificUserResponse
import com.saurabh.mediuserapp.network.response.GetUserSellHistoryResponse
import com.saurabh.mediuserapp.network.response.GetUsersOrdersResponse
import com.saurabh.mediuserapp.network.response.LoginUserResponse
import com.saurabh.mediuserapp.network.response.PasswordResetOtpResponse
import com.saurabh.mediuserapp.network.response.PasswordResetResponse
import com.saurabh.mediuserapp.network.response.RefreshTokenResponse
import com.saurabh.mediuserapp.network.response.VerifyOtpResponse
import com.saurabh.mediuserapp.utils.User
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiServices {
    // need to define end points
    // ----------------------------
    // 🔹 USER AUTHENTICATION
    // ----------------------------


    // for signup use post
    @FormUrlEncoded
    @POST("user/create")
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
    @POST("user/login")
    suspend fun loginUser(
        @Field("email") email : String,
        @Field("password") password : String
    ): Response<LoginUserResponse>


    @FormUrlEncoded
    @POST("user/verifyUserOtp")
    suspend fun verifyUserOtp(
        @Field("user_id") userId: String,
        @Field("otp") otp: String
    ): Response<VerifyOtpResponse>


    // ----------------------------
    // PASSWORD RESET
    // ----------------------------


    @FormUrlEncoded
    @POST("user/requestUserPasswordReset")
    suspend fun requestUserPasswordReset(
        @Field("email") email: String
    ): Response<PasswordResetResponse>

    @FormUrlEncoded
    @POST("user/resetUserPasswordWithOtp")
    suspend fun resetUserPasswordWithOtp(
        @Field("user_id") userId: String,
        @Field("otp") otp: String,
        @Field("new_password") newPassword: String
    ): Response<PasswordResetOtpResponse>



    // ----------------------------
    // ADMIN/USER MANAGEMENT
    // ----------------------------

    @FormUrlEncoded
    @POST("admin/user/getSpecificUser")
    suspend fun getSpecificUser(
        @Field("user_id") userId : String
    ): Response<GetSpecificUserResponse>


    // ----------------------------
    // PRODUCT MANAGEMENT
    // ----------------------------

    @GET("admin/user/getAllProducts")
    suspend fun getAllProducts(): Response<GetAllProductResponse>

    @FormUrlEncoded
    @POST("admin/user/getSpecificProduct")
    suspend fun getSpecificProduct(
        @Field("Product_id") productId: String
    ): Response<GetSpecificProductResponse>

    // ----------------------------
    // ORDER MANAGEMENT
    // ----------------------------

    @FormUrlEncoded
    @POST("user/createOrder")
    suspend fun createOrder(
        @Field("user_id") userId: String,
        @Field("Product_id") productId: String,
        @Field("quantity") quantity: String,
        @Field("message") message: String,
    ): Response<CreateOrderResponse>

    @FormUrlEncoded
    @POST("admin/user/getOrdersByUserId")
    suspend fun getUserOrders(
        @Field("user_id") userId: String
    ): Response<GetUsersOrdersResponse>

    @FormUrlEncoded
    @POST("admin/user/orderById")
    suspend fun getOrderById(
        @Field("Order_id") orderId: String
    ): Response<GetOrderByIdResponse>


    // ----------------------------
    // SELL HISTORY
    // ----------------------------


    @FormUrlEncoded
    @POST("admin/user/getSellHistoryByUserId")
    suspend fun getSellHistoryByUserId(
        @Field("user_id") userId: String
    ): Response<GetUserSellHistoryResponse>


    @POST("admin/refreshToken")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<RefreshTokenResponse>



}



