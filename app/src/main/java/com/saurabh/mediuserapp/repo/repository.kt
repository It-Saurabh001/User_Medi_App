package com.saurabh.mediuserapp.repo

import android.content.Context
import android.util.Log
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.network.ApiServices
import com.saurabh.mediuserapp.network.TokenManager
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
import javax.inject.Inject


class repository @Inject constructor(
    private val apiServices: ApiServices,
    private val tokenManager: TokenManager
){
//    private val tokenManager = TokenManager.getInstance(context)
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
    ): Flow<ResultState<CreateUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.createUser(name, password, phoneNumber, email, pinCode, address)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "createUser success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "createUser error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "createUser exception: ${e.message}")
        }
    }


    // ----------------------------------
    // LOGIN USER SAVE user_id & role (NOT tokens yet)
    // ----------------------------------
    suspend fun loginUser(
        email: String,
        password: String
    ): Flow<ResultState<LoginUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.loginUser(email, password)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                //ONLY SAVE user_id and role (tokens aayenge OTP verification ke baad)
                body.user_id?.let { tokenManager.saveUserId(it) }
                body.role.let { tokenManager.saveRole(it) }
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "loginUser success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "loginUser error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "loginUser exception: ${e.message}")
        }
    }

    // ----------------------------------
    // VERIFY USER OTP - SAVE TOKENS HERE
    // ----------------------------------
    suspend fun verifyUserOtp(
        userId: String,
        otp: String
    ): Flow<ResultState<VerifyOtpResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.verifyUserOtp(userId, otp)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // AB TOKENS SAVE KARO (according to your Flask API response)
                tokenManager.saveTokens(body.access_token.toString(), body.refresh_token.toString(), body.role.toString(), userId)
                tokenManager.saveRole(body.role.toString())
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "verifyUserOtp success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "verifyUserOtp error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "verifyUserOtp exception: ${e.message}")
        }
    }




    // ----------------------------------
    // REQUEST PASSWORD RESET OTP - NO TOKEN NEEDED
    // ----------------------------------
    suspend fun requestUserPasswordReset(
        email: String
    ): Flow<ResultState<PasswordResetOtpResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.requestUserPasswordReset(email)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "requestUserPasswordReset success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "requestUserPasswordReset error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "requestUserPasswordReset exception: ${e.message}")
        }
    }

    // ----------------------------------
    // RESET PASSWORD USING OTP - NO TOKEN NEEDED
    // ----------------------------------
    suspend fun resetUserPasswordWithOtp(
        userId: String,
        otp: String,
        newPassword: String
    ): Flow<ResultState<PasswordResetResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.resetUserPasswordWithOtp(userId, otp, newPassword)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "resetUserPasswordWithOtp success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "resetUserPasswordWithOtp error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "resetUserPasswordWithOtp exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET SPECIFIC USER (For Admin)
    // ----------------------------------
    suspend fun getSpecificUser(
        userId: String
    ): Flow<ResultState<GetSpecificUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getSpecificUser(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "getSpecificUser success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "getSpecificUser error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "getSpecificUser exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET ALL PRODUCTS
    // ----------------------------------
    suspend fun getAllProducts(): Flow<ResultState<GetAllProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getAllProducts()
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("ProductRepository", "getAppProducts success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("ProductRepository", "getAppProducts error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("ProductRepository", "getAppProducts exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET SPECIFIC PRODUCT
    // ----------------------------------

    suspend fun getSpecificProduct(productId: String): Flow<ResultState<GetSpecificProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getSpecificProduct(productId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("ProductRepository", "getSpecificProduct success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("ProductRepository", "getSpecificProduct error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("ProductRepository", "getSpecificProduct exception: ${e.message}")
        }
    }

    // ----------------------------------
    // CREATE ORDER
    // ----------------------------------
    suspend fun createOrder(
        userId: String,
        productId: String,
        quantity: String,
        message: String
    ): Flow<ResultState<CreateOrderResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.createOrder(userId, productId, quantity, message)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("OrderRepository", "createOrder success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("OrderRepository", "createOrder error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("OrderRepository", "createOrder exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET USER ORDERS
    // ----------------------------------

    suspend fun getUserOrders(userId: String): Flow<ResultState<GetUsersOrdersResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getUserOrders(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserOrderRepository", "getUserOrders success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserOrderRepository", "getUserOrders error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserOrderRepository", "getUserOrders exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET ORDER BY ID
    // ----------------------------------

    suspend fun getOrderById(orderId: String): Flow<ResultState<GetOrderByIdResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getOrderById(orderId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserOrderRepository", "getOrderById success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserOrderRepository", "getOrderById error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserOrderRepository", "getOrderById exception: ${e.message}")
        }
    }

    // ----------------------------------
    // GET SELL HISTORY BY USER ID
    // ----------------------------------

    suspend fun getSellHistoryByUserId(userId: String): Flow<ResultState<GetUserSellHistoryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getSellHistoryByUserId(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserOrderRepository", "getSellHistoryByUserId success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserOrderRepository", "getSellHistoryByUserId error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserOrderRepository", "getSellHistoryByUserId exception: ${e.message}")
        }
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


}