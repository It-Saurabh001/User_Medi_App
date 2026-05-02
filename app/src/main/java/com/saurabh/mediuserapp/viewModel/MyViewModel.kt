package com.saurabh.mediuserapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.network.TokenManager
import com.saurabh.mediuserapp.repo.Repository
import com.saurabh.mediuserapp.utils.LoginUserState
import com.saurabh.mediuserapp.utils.User
import com.saurabh.mediuserapp.utils.UserState
import com.saurabh.mediuserapp.utils.screensState.AllProductState
import com.saurabh.mediuserapp.utils.screensState.CreateOrderState
import com.saurabh.mediuserapp.utils.screensState.CreateUserState
import com.saurabh.mediuserapp.utils.screensState.GetOrderByIdState
import com.saurabh.mediuserapp.utils.screensState.GetSpecificProductState
import com.saurabh.mediuserapp.utils.screensState.OrderState
import com.saurabh.mediuserapp.utils.screensState.PasswordResetOtpState
import com.saurabh.mediuserapp.utils.screensState.PasswordResetState
import com.saurabh.mediuserapp.utils.screensState.SellHistoryState
import com.saurabh.mediuserapp.utils.screensState.VerifyOtpState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logFlow
import com.saurabh.mediuserapp.utils.logState
import com.saurabh.mediuserapp.utils.logAsOTP

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val tokenManager: TokenManager

) : ViewModel() {
    val _isUserLoggedIn = MutableStateFlow(tokenManager.isLoggedIn())
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()


    // ===========================
    // 1. AUTHENTICATION STATES
    // ===========================
    private val _createUserState = MutableStateFlow(CreateUserState())
    val createUserState = _createUserState.asStateFlow()

    private val _loginUserState = MutableStateFlow(LoginUserState())
    val loginUserState = _loginUserState.asStateFlow()

    private val _specificUserState = MutableStateFlow(UserState())
    val specificUserState = _specificUserState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow(VerifyOtpState())
    val verifyOtpState = _verifyOtpState.asStateFlow()

    private val _passwordResetOtpState = MutableStateFlow(PasswordResetOtpState())
    val passwordResetOtpState = _passwordResetOtpState.asStateFlow()

    private val _passwordResetState = MutableStateFlow(PasswordResetState())
    val passwordResetState = _passwordResetState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    // ===========================
    // 2. PRODUCT STATES
    // ===========================

    private val _getAllProduct = MutableStateFlow(AllProductState())
    val getAllProduct = _getAllProduct.asStateFlow()

    private val _specificProductState = MutableStateFlow(GetSpecificProductState())
    val specificProductState = _specificProductState.asStateFlow()

    // ===========================
    // 3. ORDER STATES
    // ===========================
    private val _createOrderState = MutableStateFlow(CreateOrderState())
    val createOrderState = _createOrderState.asStateFlow()

    private val _userOrdersState = MutableStateFlow(OrderState())
    val userOrdersState = _userOrdersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow(GetOrderByIdState())
    val orderDetailState = _orderDetailState.asStateFlow()

    private val _sellHistoryState = MutableStateFlow(SellHistoryState())
    val sellHistoryState = _sellHistoryState.asStateFlow()

    // ===========================
    // AUTH FUNCTIONS
    // ===========================

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        _isUserLoggedIn.value = tokenManager.isLoggedIn()
        Log.d("TAG", "checkLoginStatus: isLoggedIn = ${_isUserLoggedIn.value}")
    }

    fun setUserLoggedIn() {
        _isUserLoggedIn.value = true
        Log.d("TAG", "setUserLoggedIn: User logged in, isLoggedIn set to true")
    }

    fun setUserLoggedOut() {
        tokenManager.clearTokens()
        _isUserLoggedIn.value = false
        Log.d("TAG", "setUserLoggedOut: User logged out and tokens cleared")
    }


    fun createUser(
        name: String,
        password: String,
        phoneNumber: String,
        email: String,
        pinCode: String,
        address: String,
        role: String
    ) {
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [createUser] called with name: $name, email: $email")
        viewModelScope.launch(Dispatchers.IO) {
            repository.createUser(name, password, phoneNumber, email, pinCode, address)
                .logFlow("createUser")
                .collect {
                it.logState("createUser")
                when (it) {
                    is ResultState.Loading -> {
                        _createUserState.value = CreateUserState(isLoading = true)
                    }

                    is ResultState.Error -> {
                        _createUserState.value =
                            CreateUserState(error = it.exception.message, isLoading = false)
                    }

                    is ResultState.Success -> {
                        _createUserState.value =
                            CreateUserState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun loginUser(email: String, password: String) {
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [loginUser] called for email: $email")
        viewModelScope.launch(Dispatchers.IO) {
            repository.loginUser(email, password)
                .logFlow("loginUser")
                .collect {
                it.logState("loginUser")
                when (it) {
                    is ResultState.Loading -> {
                        _loginUserState.value = LoginUserState(isLoading = true)
                    }

                    is ResultState.Error -> {
                        _loginUserState.value =
                            LoginUserState(error = it.exception.message, isLoading = false)
                    }

                    is ResultState.Success -> {
                        _loginUserState.value = LoginUserState(success = it.data, isLoading = false)

                    }
                }
            }
        }
    }

    // New: Verify OTP (This actually saves the token in Repo)
    fun verifyUserOtp(userId: String, otp: String) {
        otp.logAsOTP("verifyUserOtp")
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [verifyUserOtp] called for userId: $userId")
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyUserOtp(userId, otp)
                .logFlow("verifyUserOtp")
                .collect { result ->
                result.logState("verifyUserOtp")
                when (result) {
                    is ResultState.Loading -> {
                        _verifyOtpState.value = VerifyOtpState(isLoading = true)
                    }

                    is ResultState.Error -> {
                        _verifyOtpState.value = VerifyOtpState(error = result.exception.message)
                    }

                    is ResultState.Success -> {
                        val data = result.data
                        tokenManager.saveTokens(
                            accessToken = data.access_token ?: "",
                            refreshToken = data.refresh_token ?: "",
                            role = data.role ?: "",
                            userId = userId
                        )
                        setUserLoggedIn()
                        _verifyOtpState.value = VerifyOtpState(success = result.data)
                    }
                }
            }
        }
    }

    // New: Request Password Reset
    fun requestPasswordReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestUserPasswordReset(email).collect { result ->
                when (result) {
                    is ResultState.Loading -> _passwordResetState.value =
                        PasswordResetState(isLoading = true)

                    is ResultState.Error -> _passwordResetState.value =
                        PasswordResetState(error = result.exception.message)

                    is ResultState.Success -> _passwordResetState.value =
                        PasswordResetState(success = result.data)
                }
            }
        }
    }

    // New: Request Password Reset
    fun resetPasswordOtp(userId: String, otp: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetUserPasswordWithOtp(userId, otp, newPassword).collect { result ->
                when (result) {
                    is ResultState.Loading -> _passwordResetOtpState.value =
                        PasswordResetOtpState(isLoading = true)

                    is ResultState.Error -> _passwordResetOtpState.value =
                        PasswordResetOtpState(error = result.exception.message)

                    is ResultState.Success -> _passwordResetOtpState.value =
                        PasswordResetOtpState(success = result.data)
                }
            }
        }
    }


    fun getSpecificUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSpecificUser(userId).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _specificUserState.value = UserState(isLoading = true)
                    }

                    is ResultState.Error -> {
                        _specificUserState.value =
                            UserState(error = it.exception.message, isLoading = false)
                        Log.d(
                            "TAG",
                            "getSpecificUser:  viewmodel error ${_specificUserState.value.error}"
                        )
                    }

                    is ResultState.Success -> {
                        _specificUserState.value = UserState(success = it.data, isLoading = false)
                        Log.d(
                            "TAG",
                            "getSpecificUser:  viewmodel success ${_specificUserState.value.success!!.message}"
                        )
                    }

                }
            }

        }
    }

    // ===========================
    // PRODUCT FUNCTIONS
    // ===========================

    fun getAllProduct() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProducts().collect { result ->
                when (result) {
                    is ResultState.Loading -> _getAllProduct.value =
                        AllProductState(isLoading = true)

                    is ResultState.Error -> _getAllProduct.value =
                        AllProductState(error = result.exception.message)

                    is ResultState.Success -> _getAllProduct.value =
                        AllProductState(success = result.data)
                }
            }
        }
    }

    fun getSpecificProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSpecificProduct(productId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _specificProductState.value =
                        GetSpecificProductState(isLoading = true)

                    is ResultState.Error -> _specificProductState.value =
                        GetSpecificProductState(error = result.exception.message)

                    is ResultState.Success -> _specificProductState.value =
                        GetSpecificProductState(success = result.data)
                }
            }
        }
    }

    // ===========================
    // HELPER FUNCTIONS
    // ===========================

    fun getStoredToken(): String {
        return repository.getStoredToken()
    }

    fun getCurrentUserId(): String? {
        return repository.getCurrentUserId()
    }

    fun getCurrentUserRole(): String? {
        return repository.getCurrentUserRole()
    }

    fun logout() {
        repository.logout()
        setUserLoggedOut()
        Log.d("MyViewModel", "User logged out successfully")
    }
}