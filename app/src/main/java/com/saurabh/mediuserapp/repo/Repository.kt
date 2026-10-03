package com.saurabh.mediuserapp.repo

import android.util.Log
import com.google.gson.Gson
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.network.ApiServices
import com.saurabh.mediuserapp.network.MainApiService
import com.saurabh.mediuserapp.network.TokenManager
import com.saurabh.mediuserapp.network.response.ApiErrorResponse
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
import com.saurabh.mediuserapp.network.response.VerifyOtpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    @MainApiService private val apiServices: ApiServices,
    private val tokenManager: TokenManager
) {
    private fun parseErrorMessage(rawJson: String?): String? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            val errorResponse = Gson().fromJson(rawJson, ApiErrorResponse::class.java)
            errorResponse?.displayMessage ?: rawJson
        } catch (_: Exception) {
            rawJson
        }
    }

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Flow<ResultState<T>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(ResultState.Success(body))
                } else {
                    emit(ResultState.Error(Exception("Empty response body with HTTP ${response.code()}")))
                }
            } else {
                val rawError = response.errorBody()?.string()
                val parsedMessage = parseErrorMessage(rawError) ?: "Request failed with HTTP ${response.code()}"
                emit(ResultState.Error(Exception(parsedMessage)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }

    // ----------------------------------
    // CREATE USER (Signup)
    // ----------------------------------
    suspend fun createUser(
        name: String,
        password: String,
        phoneNumber: String,
        email: String,
        pinCode: String,
        address: String
    ): Flow<ResultState<CreateUserResponse>> = safeApiCall {
        apiServices.createUser(name, password, phoneNumber, email, pinCode, address)
    }

    // ----------------------------------
    // LOGIN USER SAVE user_id & role (NOT tokens yet)
    // ----------------------------------
    suspend fun loginUser(
        email: String,
        password: String
    ): Flow<ResultState<LoginUserResponse>> = flow {
        safeApiCall { apiServices.loginUser(email, password) }.collect { result ->
            if (result is ResultState.Success) {
                result.data.user_id?.let { tokenManager.saveUserId(it) }
                val role = result.data.role ?: "user"
                tokenManager.saveRole(role)
            }
            emit(result)
        }
    }

    // ----------------------------------
    // VERIFY USER OTP - SAVE TOKENS HERE
    // ----------------------------------
    suspend fun verifyUserOtp(
        userId: String,
        otp: String
    ): Flow<ResultState<VerifyOtpResponse>> = flow {
        safeApiCall { apiServices.verifyUserOtp(userId, otp) }.collect { result ->
            if (result is ResultState.Success) {
                val accessToken = result.data.access_token ?: ""
                val refreshToken = result.data.refresh_token ?: ""
                val role = result.data.role ?: "user"
                tokenManager.saveTokens(accessToken, refreshToken, role, userId)
                tokenManager.saveRole(role)
            }
            emit(result)
        }
    }

    // ----------------------------------
    // REQUEST PASSWORD RESET OTP - NO TOKEN NEEDED
    // ----------------------------------
    suspend fun requestUserPasswordReset(
        email: String
    ): Flow<ResultState<PasswordResetResponse>> = safeApiCall {
        apiServices.requestUserPasswordReset(email)
    }

    // ----------------------------------
    // RESET PASSWORD USING OTP - NO TOKEN NEEDED
    // ----------------------------------
    suspend fun resetUserPasswordWithOtp(
        userId: String,
        otp: String,
        newPassword: String
    ): Flow<ResultState<PasswordResetOtpResponse>> = safeApiCall {
        apiServices.resetUserPasswordWithOtp(userId, otp, newPassword)
    }

    // ----------------------------------
    // GET SPECIFIC USER (For Admin / Self Profile)
    // ----------------------------------
    suspend fun getSpecificUser(
        userId: String
    ): Flow<ResultState<GetSpecificUserResponse>> = safeApiCall {
        apiServices.getSpecificUser(userId)
    }

    // ----------------------------------
    // GET ALL PRODUCTS
    // ----------------------------------
    suspend fun getAllProducts(): Flow<ResultState<GetAllProductResponse>> = safeApiCall {
        apiServices.getAllProducts()
    }

    // ----------------------------------
    // GET SPECIFIC PRODUCT
    // ----------------------------------
    suspend fun getSpecificProduct(
        productId: String
    ): Flow<ResultState<GetSpecificProductResponse>> = safeApiCall {
        apiServices.getSpecificProduct(productId)
    }

    // ----------------------------------
    // CREATE ORDER
    // ----------------------------------
    suspend fun createOrder(
        userId: String,
        productId: String,
        quantity: String,
        message: String
    ): Flow<ResultState<CreateOrderResponse>> = safeApiCall {
        apiServices.createOrder(userId, productId, quantity, message)
    }

    // ----------------------------------
    // GET USER ORDERS
    // ----------------------------------
    suspend fun getUserOrders(
        userId: String
    ): Flow<ResultState<GetUsersOrdersResponse>> = safeApiCall {
        apiServices.getUserOrders(userId)
    }

    // ----------------------------------
    // GET ORDER BY ID
    // ----------------------------------
    suspend fun getOrderById(
        orderId: String
    ): Flow<ResultState<GetOrderByIdResponse>> = safeApiCall {
        apiServices.getOrderById(orderId)
    }

    // ----------------------------------
    // GET SELL HISTORY BY USER ID
    // ----------------------------------
    suspend fun getSellHistoryByUserId(
        userId: String
    ): Flow<ResultState<GetUserSellHistoryResponse>> = safeApiCall {
        apiServices.getSellHistoryByUserId(userId)
    }

    fun logout() {
        tokenManager.clearTokens()
        Log.d("UserRepository", "User logged out")
    }

    fun isUserLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }

    fun getCurrentUserRole(): String? {
        return tokenManager.getUserRole()
    }

    fun getStoredToken(): String {
        return tokenManager.getAccessToken() ?: ""
    }
}