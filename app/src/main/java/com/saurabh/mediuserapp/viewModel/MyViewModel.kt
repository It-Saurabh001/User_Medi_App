package com.saurabh.mediuserapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediuserapp.BuildConfig
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.data.v1.core.AppEvent
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import com.saurabh.mediuserapp.data.v1.core.IdempotencyKeyProvider
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.core.PageRequest
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.core.hasMore
import com.saurabh.mediuserapp.data.v1.dto.AppConfig
import com.saurabh.mediuserapp.data.v1.dto.InvoiceDto
import com.saurabh.mediuserapp.data.v1.dto.OrderCreateRequest
import com.saurabh.mediuserapp.data.v1.dto.ProfileUpdateRequest
import com.saurabh.mediuserapp.data.v1.dto.UserCreateRequest
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.repo.AppException
import com.saurabh.mediuserapp.repo.FilePart
import com.saurabh.mediuserapp.repo.ImagePart
import com.saurabh.mediuserapp.repo.OrderFilter
import com.saurabh.mediuserapp.repo.PagedData
import com.saurabh.mediuserapp.repo.ProductFilter
import com.saurabh.mediuserapp.repo.Repository
import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.LoginUserState
import com.saurabh.mediuserapp.utils.User
import com.saurabh.mediuserapp.utils.UserState
import com.saurabh.mediuserapp.utils.logAsOTP
import com.saurabh.mediuserapp.utils.screensState.AllProductState
import com.saurabh.mediuserapp.utils.screensState.CancelOrderState
import com.saurabh.mediuserapp.utils.screensState.CreateOrderState
import com.saurabh.mediuserapp.utils.screensState.CreateUserState
import com.saurabh.mediuserapp.utils.screensState.GetOrderByIdState
import com.saurabh.mediuserapp.utils.screensState.GetSpecificProductState
import com.saurabh.mediuserapp.utils.screensState.GetUserSellHistoryState
import com.saurabh.mediuserapp.utils.screensState.GetUsersOrdersState
import com.saurabh.mediuserapp.utils.screensState.PasswordResetOtpState
import com.saurabh.mediuserapp.utils.screensState.PasswordResetState
import com.saurabh.mediuserapp.utils.screensState.UpdateProfileState
import com.saurabh.mediuserapp.utils.screensState.UploadPrescriptionState
import com.saurabh.mediuserapp.utils.screensState.VerifyOtpState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val appEvents: AppEvents,
    private val idempotencyKeyProvider: IdempotencyKeyProvider
) : ViewModel() {
    val _isUserLoggedIn = MutableStateFlow(repository.isLoggedIn())
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    // ===========================
    // APP-LEVEL STATES / EVENTS
    // ===========================
    private val _upgradeRequiredVersion = MutableStateFlow<String?>(null)

    /** Non-null = show the blocking "update the app" screen (value = minimum version or "latest"). */
    val upgradeRequiredVersion: StateFlow<String?> = _upgradeRequiredVersion.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** One-shot snackbar messages (rate limited, account blocked). */
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _appConfigState = MutableStateFlow<UiState<AppConfig>>(UiState.Loading)
    val appConfigState = _appConfigState.asStateFlow()

    // ===========================
    // 1. AUTHENTICATION STATES
    // ===========================
    private val _createUserState = MutableStateFlow(CreateUserState())
    val createUserState = _createUserState.asStateFlow()

    private val _loginUserState = MutableStateFlow(LoginUserState())
    val loginUserState = _loginUserState.asStateFlow()

    private val _specificUserState = MutableStateFlow(UserState())
    val specificUserState = _specificUserState.asStateFlow()

    private val _updateProfileState = MutableStateFlow(UpdateProfileState())
    val updateProfileState = _updateProfileState.asStateFlow()

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

    private var productFilter = ProductFilter()
    private var productRequest = PageRequest()

    // ===========================
    // 3. ORDER STATES
    // ===========================
    private val _createOrderState = MutableStateFlow(CreateOrderState())
    val createOrderState = _createOrderState.asStateFlow()

    private val _userOrdersState = MutableStateFlow(GetUsersOrdersState())
    val userOrdersState = _userOrdersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow(GetOrderByIdState())
    val orderDetailState = _orderDetailState.asStateFlow()

    private val _sellHistoryState = MutableStateFlow(GetUserSellHistoryState())
    val sellHistoryState = _sellHistoryState.asStateFlow()

    private val _cancelOrderState = MutableStateFlow(CancelOrderState())
    val cancelOrderState = _cancelOrderState.asStateFlow()

    private val _uploadPrescriptionState = MutableStateFlow(UploadPrescriptionState())
    val uploadPrescriptionState = _uploadPrescriptionState.asStateFlow()

    private val _invoiceState = MutableStateFlow<UiState<InvoiceDto>>(UiState.Loading)
    val invoiceState = _invoiceState.asStateFlow()

    private var orderFilter = OrderFilter()
    private var orderRequest = PageRequest()

    // ===========================
    // HELPERS
    // ===========================

    /** Collects a repository flow into UI state; AppError is passed through for form/field handling. */
    private suspend inline fun <T> Flow<ResultState<T>>.collectInto(
        crossinline onLoading: () -> Unit,
        crossinline onError: (String?, AppError?) -> Unit,
        crossinline onSuccess: (T) -> Unit
    ) = collect { result ->
        when (result) {
            is ResultState.Loading -> onLoading()
            is ResultState.Error -> {
                val appError = (result.exception as? AppException)?.error
                onError(appError?.userMessage() ?: result.exception.message, appError)
            }
            is ResultState.Success -> onSuccess(result.data)
        }
    }

    // ===========================
    // AUTH FUNCTIONS
    // ===========================

    init {
        checkLoginStatus()
        collectAppEvents()
        getAppConfig()
    }

    private fun collectAppEvents() {
        viewModelScope.launch {
            appEvents.events.collect { event ->
                when (event) {
                    is AppEvent.SessionInvalid -> {
                        resetAllState()
                        repository.clearSession()
                        _isUserLoggedIn.value = false
                    }
                    is AppEvent.AccountBlocked -> {
                        _messages.tryEmit("Your account has been blocked. Please contact the pharmacy.")
                        logout()
                    }
                    is AppEvent.RateLimited ->
                        _messages.tryEmit("Too many attempts. Please wait a moment and try again.")
                    is AppEvent.UpgradeRequired ->
                        _upgradeRequiredVersion.value = event.minVersion ?: "latest"
                }
            }
        }
    }

    private fun resetAllState() {
        _createUserState.value = CreateUserState()
        _loginUserState.value = LoginUserState()
        _specificUserState.value = UserState()
        _updateProfileState.value = UpdateProfileState()
        _verifyOtpState.value = VerifyOtpState()
        _passwordResetOtpState.value = PasswordResetOtpState()
        _passwordResetState.value = PasswordResetState()
        _getAllProduct.value = AllProductState()
        _specificProductState.value = GetSpecificProductState()
        _createOrderState.value = CreateOrderState()
        _userOrdersState.value = GetUsersOrdersState()
        _orderDetailState.value = GetOrderByIdState()
        _sellHistoryState.value = GetUserSellHistoryState()
        _cancelOrderState.value = CancelOrderState()
        _uploadPrescriptionState.value = UploadPrescriptionState()
    }

    fun checkLoginStatus() {
        _isUserLoggedIn.value = repository.isLoggedIn()
        Log.d("TAG", "checkLoginStatus: isLoggedIn = ${_isUserLoggedIn.value}")
    }

    fun setUserLoggedIn() {
        _isUserLoggedIn.value = true
        Log.d("TAG", "setUserLoggedIn: User logged in, isLoggedIn set to true")
    }

    fun setUserLoggedOut() {
        repository.clearSession()
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
        image: ImagePart? = null
    ) {
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [createUser] called with name: $name, email: $email")
        viewModelScope.launch(Dispatchers.IO) {
            val request = UserCreateRequest(
                name = name,
                email = email,
                password = password,
                phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                address = address.takeIf { it.isNotBlank() },
                pinCode = pinCode.takeIf { it.isNotBlank() }
            )
            repository.signup(request, image).collectInto(
                onLoading = { _createUserState.value = CreateUserState(isLoading = true) },
                onError = { msg, err -> _createUserState.value = CreateUserState(error = msg, appError = err) },
                onSuccess = { _createUserState.value = CreateUserState(success = it) }
            )
        }
    }

    fun loginUser(email: String, password: String) {
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [loginUser] called for email: $email")
        viewModelScope.launch(Dispatchers.IO) {
            repository.login(email, password).collectInto(
                onLoading = { _loginUserState.value = LoginUserState(isLoading = true) },
                onError = { msg, err -> _loginUserState.value = LoginUserState(error = msg, appError = err) },
                onSuccess = { _loginUserState.value = LoginUserState(success = it) }
            )
        }
    }

    // Verify OTP (the Repository saves the session; role gate happens here)
    fun verifyUserOtp(userId: String, otp: String) {
        otp.logAsOTP("verifyUserOtp")
        AppLogger.d(AppLogger.TAG_VIEWMODEL, "Function [verifyUserOtp] called for userId: $userId")
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyOtp(userId, otp).collectInto(
                onLoading = { _verifyOtpState.value = VerifyOtpState(isLoading = true) },
                onError = { msg, err -> _verifyOtpState.value = VerifyOtpState(error = msg, appError = err) },
                onSuccess = { data ->
                    if (data.role != "user") {
                        viewModelScope.launch { repository.logout().collect { } }
                        _isUserLoggedIn.value = false
                        _verifyOtpState.value = VerifyOtpState(error = "Customer account required")
                    } else {
                        setUserLoggedIn()
                        _verifyOtpState.value = VerifyOtpState(success = data)
                    }
                }
            )
        }
    }

    // Request Password Reset OTP
    fun requestPasswordReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestPasswordReset(email).collectInto(
                onLoading = { _passwordResetState.value = PasswordResetState(isLoading = true) },
                onError = { msg, err -> _passwordResetState.value = PasswordResetState(error = msg, appError = err) },
                onSuccess = { _passwordResetState.value = PasswordResetState(success = it) }
            )
        }
    }

    // Confirm password reset (email is the value carried as `userId` by the reset screens)
    fun resetPasswordOtp(email: String, otp: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.confirmPasswordReset(email, otp, newPassword).collectInto(
                onLoading = { _passwordResetOtpState.value = PasswordResetOtpState(isLoading = true) },
                onError = { msg, err -> _passwordResetOtpState.value = PasswordResetOtpState(error = msg, appError = err) },
                onSuccess = { _passwordResetOtpState.value = PasswordResetOtpState(success = it) }
            )
        }
    }

    fun getSpecificUser() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMe().collectInto(
                onLoading = { _specificUserState.value = UserState(isLoading = true) },
                onError = { msg, _ -> _specificUserState.value = UserState(error = msg) },
                onSuccess = { _specificUserState.value = UserState(success = it) }
            )
        }
    }

    fun updateMe(request: ProfileUpdateRequest, image: ImagePart? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMe(request, image).collectInto(
                onLoading = { _updateProfileState.value = UpdateProfileState(isLoading = true) },
                onError = { msg, err -> _updateProfileState.value = UpdateProfileState(error = msg, appError = err) },
                onSuccess = {
                    _updateProfileState.value = UpdateProfileState(success = it)
                    _specificUserState.value = UserState(success = it)
                }
            )
        }
    }

    // ===========================
    // PRODUCT FUNCTIONS
    // ===========================

    fun getAllProduct(filter: ProductFilter = ProductFilter()) {
        productFilter = filter
        productRequest = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProducts(productFilter, productRequest).collectInto(
                onLoading = { _getAllProduct.value = AllProductState(isLoading = true) },
                onError = { msg, err -> _getAllProduct.value = AllProductState(error = msg, appError = err) },
                onSuccess = {
                    _getAllProduct.value = AllProductState(
                        success = it, pagination = it.pagination, endReached = it.pagination?.hasMore() != true
                    )
                }
            )
        }
    }

    fun loadMoreProducts() {
        val current = _getAllProduct.value
        if (current.isLoading || current.isLoadingMore || current.endReached) return
        val next = productRequest.next(current.pagination)
        if (next == null) {
            _getAllProduct.value = current.copy(endReached = true)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProducts(productFilter, next).collectInto(
                onLoading = { _getAllProduct.value = current.copy(isLoadingMore = true) },
                onError = { msg, err ->
                    _getAllProduct.value = current.copy(isLoadingMore = false, error = msg, appError = err)
                },
                onSuccess = { page ->
                    productRequest = next
                    _getAllProduct.value = AllProductState(
                        success = PagedData(current.success?.items.orEmpty() + page.items, page.pagination),
                        pagination = page.pagination,
                        endReached = page.pagination?.hasMore() != true
                    )
                }
            )
        }
    }

    fun getSpecificProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProduct(productId).collectInto(
                onLoading = { _specificProductState.value = GetSpecificProductState(isLoading = true) },
                onError = { msg, _ -> _specificProductState.value = GetSpecificProductState(error = msg) },
                onSuccess = { _specificProductState.value = GetSpecificProductState(success = it) }
            )
        }
    }

    // ===========================
    // ORDER FUNCTIONS
    // ===========================

    /** Idempotency key is created once per checkout, reused on retry, cleared on success/reset. */
    fun createOrder(
        productId: String,
        quantity: String,
        message: String,
        doctorName: String? = null,
        patientName: String? = null,
        prescription: FilePart? = null
    ) {
        val key = _createOrderState.value.idempotencyKey ?: idempotencyKeyProvider.newKey()
        viewModelScope.launch(Dispatchers.IO) {
            val request = OrderCreateRequest(
                productId = productId,
                quantity = quantity.toIntOrNull() ?: 0,
                message = message.takeIf { it.isNotBlank() },
                doctorName = doctorName?.takeIf { it.isNotBlank() },
                patientName = patientName?.takeIf { it.isNotBlank() }
            )
            repository.createOrder(request, prescription, key).collectInto(
                onLoading = { _createOrderState.value = CreateOrderState(isLoading = true, idempotencyKey = key) },
                onError = { msg, err ->
                    _createOrderState.value = CreateOrderState(error = msg, appError = err, idempotencyKey = key)
                },
                onSuccess = {
                    _createOrderState.value = CreateOrderState(success = it)
                    // Refresh user orders automatically after successful creation
                    getUserOrders()
                }
            )
        }
    }

    fun getUserOrders(filter: OrderFilter = OrderFilter()) {
        orderFilter = filter
        orderRequest = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(orderFilter, orderRequest).collectInto(
                onLoading = { _userOrdersState.value = GetUsersOrdersState(isLoading = true) },
                onError = { msg, err -> _userOrdersState.value = GetUsersOrdersState(error = msg, appError = err) },
                onSuccess = {
                    _userOrdersState.value = GetUsersOrdersState(
                        success = it, pagination = it.pagination, endReached = it.pagination?.hasMore() != true
                    )
                }
            )
        }
    }

    fun loadMoreOrders() {
        val current = _userOrdersState.value
        if (current.isLoading || current.isLoadingMore || current.endReached) return
        val next = orderRequest.next(current.pagination)
        if (next == null) {
            _userOrdersState.value = current.copy(endReached = true)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(orderFilter, next).collectInto(
                onLoading = { _userOrdersState.value = current.copy(isLoadingMore = true) },
                onError = { msg, err ->
                    _userOrdersState.value = current.copy(isLoadingMore = false, error = msg, appError = err)
                },
                onSuccess = { page ->
                    orderRequest = next
                    _userOrdersState.value = GetUsersOrdersState(
                        success = PagedData(current.success?.items.orEmpty() + page.items, page.pagination),
                        pagination = page.pagination,
                        endReached = page.pagination?.hasMore() != true
                    )
                }
            )
        }
    }

    fun getOrderById(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrder(orderId).collectInto(
                onLoading = { _orderDetailState.value = GetOrderByIdState(isLoading = true) },
                onError = { msg, _ -> _orderDetailState.value = GetOrderByIdState(error = msg) },
                onSuccess = { _orderDetailState.value = GetOrderByIdState(success = it) }
            )
        }
    }

    /** v1 has no sell-history endpoint: sold orders of the logged-in customer. */
    fun getSellHistoryByUserId() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(OrderFilter(status = OrderStatus.SOLD), PageRequest()).collectInto(
                onLoading = { _sellHistoryState.value = GetUserSellHistoryState(isLoading = true) },
                onError = { msg, _ -> _sellHistoryState.value = GetUserSellHistoryState(error = msg) },
                onSuccess = { _sellHistoryState.value = GetUserSellHistoryState(success = it) }
            )
        }
    }

    fun cancelOrder(orderId: String, reason: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cancelOrder(orderId, reason?.takeIf { it.isNotBlank() }).collectInto(
                onLoading = { _cancelOrderState.value = CancelOrderState(isLoading = true) },
                onError = { msg, err -> _cancelOrderState.value = CancelOrderState(error = msg, appError = err) },
                onSuccess = {
                    _cancelOrderState.value = CancelOrderState(success = it)
                    _orderDetailState.value = GetOrderByIdState(success = it)
                    getUserOrders(orderFilter)
                }
            )
        }
    }

    fun uploadPrescription(
        orderId: String,
        file: FilePart,
        doctorName: String? = null,
        patientName: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.uploadPrescription(orderId, file, doctorName, patientName).collectInto(
                onLoading = { _uploadPrescriptionState.value = UploadPrescriptionState(isLoading = true) },
                onError = { msg, err ->
                    _uploadPrescriptionState.value = UploadPrescriptionState(error = msg, appError = err)
                },
                onSuccess = {
                    _uploadPrescriptionState.value = UploadPrescriptionState(success = it)
                    _orderDetailState.value = GetOrderByIdState(success = it)
                }
            )
        }
    }

    fun getInvoice(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getInvoice(orderId).collectInto(
                onLoading = { _invoiceState.value = UiState.Loading },
                onError = { _, err -> _invoiceState.value = UiState.Error(err ?: AppError.Unknown(null)) },
                onSuccess = { _invoiceState.value = UiState.Success(it) }
            )
        }
    }

    // ===========================
    // APP CONFIG
    // ===========================

    fun getAppConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAppConfig().collectInto(
                onLoading = { _appConfigState.value = UiState.Loading },
                onError = { _, err -> _appConfigState.value = UiState.Error(err ?: AppError.Unknown(null)) },
                onSuccess = {
                    _appConfigState.value = UiState.Success(it)
                    val min = it.minAppVersion
                    if (!min.isNullOrBlank() && isOlderVersion(BuildConfig.VERSION_NAME, min)) {
                        _upgradeRequiredVersion.value = min
                    }
                }
            )
        }
    }

    /** Compares dotted numeric versions ("1.4.0" < "2.0.0"). */
    private fun isOlderVersion(current: String, minimum: String): Boolean {
        val a = current.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val b = minimum.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x < y
        }
        return false
    }

    // ===========================
    // RESET FUNCTIONS FOR ONE-TIME EVENTS
    // ===========================

    fun resetCreateUserState() {
        _createUserState.value = CreateUserState()
    }

    fun resetLoginState() {
        _loginUserState.value = LoginUserState()
    }

    fun resetVerifyOtpState() {
        _verifyOtpState.value = VerifyOtpState()
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState()
    }

    fun resetPasswordResetOtpState() {
        _passwordResetOtpState.value = PasswordResetOtpState()
    }

    /** Also drops the idempotency key (new checkout). */
    fun resetCreateOrderState() {
        _createOrderState.value = CreateOrderState()
    }

    // ===========================
    // HELPER FUNCTIONS
    // ===========================

    fun getCurrentUserId(): String? {
        return repository.currentAccountId()
    }

    fun getCurrentUserRole(): String? {
        return repository.currentRole()
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logout().collect { }
            setUserLoggedOut()
            Log.d("MyViewModel", "User logged out successfully")
        }
    }
}
