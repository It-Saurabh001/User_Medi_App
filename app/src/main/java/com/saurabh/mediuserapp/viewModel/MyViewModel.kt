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
import com.saurabh.mediuserapp.data.v1.dto.ProductCategoryDto
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
import com.saurabh.mediuserapp.utils.screensState.BarcodeLookupState
import com.saurabh.mediuserapp.utils.screensState.CancelOrderState
import com.saurabh.mediuserapp.utils.screensState.CreateOrderState
import com.saurabh.mediuserapp.utils.screensState.CreateUserState
import com.saurabh.mediuserapp.utils.screensState.EditOrderState
import com.saurabh.mediuserapp.utils.screensState.GetOrderByIdState
import com.saurabh.mediuserapp.utils.screensState.GetSpecificProductState
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

    private val _accountBlocked = MutableStateFlow(false)

    /** True after a 403 ACCOUNT_BLOCKED from any call; NavApp shows AccountBlockedScreen. */
    val accountBlocked: StateFlow<Boolean> = _accountBlocked.asStateFlow()

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

    private val _productFilter = MutableStateFlow(ProductFilter())
    val productFilter: StateFlow<ProductFilter> = _productFilter.asStateFlow()

    /** Product list sort (`sort` per products allow-list, `order` asc/desc). */
    private val _productSort = MutableStateFlow(PageRequest(sort = "name", order = "asc"))
    val productSort: StateFlow<PageRequest> = _productSort.asStateFlow()
    private var productRequest = PageRequest()

    private val _categoriesState = MutableStateFlow<UiState<List<ProductCategoryDto>>>(UiState.Loading)
    val categoriesState = _categoriesState.asStateFlow()

    // ===========================
    // 3. ORDER STATES
    // ===========================
    private val _createOrderState = MutableStateFlow(CreateOrderState())
    val createOrderState = _createOrderState.asStateFlow()

    private val _userOrdersState = MutableStateFlow(GetUsersOrdersState())
    val userOrdersState = _userOrdersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow(GetOrderByIdState())
    val orderDetailState = _orderDetailState.asStateFlow()

    private val _historyState = MutableStateFlow(GetUsersOrdersState())
    val historyState = _historyState.asStateFlow()

    private val _historyFilter = MutableStateFlow(OrderFilter(status = OrderStatus.SOLD))
    val historyFilter: StateFlow<OrderFilter> = _historyFilter.asStateFlow()
    private var historyRequest = PageRequest()

    private val _editOrderState = MutableStateFlow(EditOrderState())
    val editOrderState = _editOrderState.asStateFlow()

    private val _cancelOrderState = MutableStateFlow(CancelOrderState())
    val cancelOrderState = _cancelOrderState.asStateFlow()

    private val _uploadPrescriptionState = MutableStateFlow(UploadPrescriptionState())
    val uploadPrescriptionState = _uploadPrescriptionState.asStateFlow()

    private val _invoiceState = MutableStateFlow<UiState<InvoiceDto>>(UiState.Loading)
    val invoiceState = _invoiceState.asStateFlow()

    private val _orderFilter = MutableStateFlow(OrderFilter())
    val orderFilter: StateFlow<OrderFilter> = _orderFilter.asStateFlow()
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
                    is AppEvent.AccountBlocked -> _accountBlocked.value = true
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
        _historyState.value = GetUsersOrdersState()
        _cancelOrderState.value = CancelOrderState()
        _editOrderState.value = EditOrderState()
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

    fun resetUpdateProfileState() { _updateProfileState.value = UpdateProfileState() }

    fun updateMe(request: ProfileUpdateRequest, image: ImagePart? = null) {
        if (_updateProfileState.value.isLoading) return
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

    fun getAllProduct(filter: ProductFilter = _productFilter.value, force: Boolean = false) {
        val current = _getAllProduct.value
        if (!force && filter == _productFilter.value && (current.isLoading || current.success != null)) return
        _productFilter.value = filter
        productRequest = _productSort.value.copy(offset = 0)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProducts(filter, productRequest).collectInto(
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
            repository.getProducts(_productFilter.value, next).collectInto(
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

    /** Applies a new filter and/or sort and reloads page 1. */
    fun setProductFilter(
        filter: ProductFilter = _productFilter.value,
        sort: String = _productSort.value.sort ?: "name",
        order: String = _productSort.value.order ?: "asc",
    ) {
        _productSort.value = PageRequest(sort = sort, order = order)
        getAllProduct(filter, force = true)
    }

    fun loadProductCategories(force: Boolean = false) {
        if (!force && _categoriesState.value is UiState.Success) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProductCategories(PageRequest(limit = 100, sort = "name", order = "asc")).collectInto(
                onLoading = { if (_categoriesState.value !is UiState.Success) _categoriesState.value = UiState.Loading },
                onError = { _, err -> if (_categoriesState.value !is UiState.Success) _categoriesState.value = UiState.Error(err ?: AppError.Unknown(null)) },
                onSuccess = { _categoriesState.value = UiState.Success(it.items) }
            )
        }
    }

    private val _barcodeLookup = MutableStateFlow<BarcodeLookupState>(BarcodeLookupState.Idle)
    val barcodeLookup: StateFlow<BarcodeLookupState> = _barcodeLookup.asStateFlow()
    private var barcodeJob: kotlinx.coroutines.Job? = null

    fun lookupBarcode(barcode: String) {
        val code = barcode.trim()
        if (code.isEmpty() || _barcodeLookup.value is BarcodeLookupState.Loading) return
        barcodeJob?.cancel()
        barcodeJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getProductByBarcode(code).collectInto(
                onLoading = { _barcodeLookup.value = BarcodeLookupState.Loading(code) },
                onError = { m, e ->
                    _barcodeLookup.value = if ((e as? AppError.Api)?.httpStatus == 404) BarcodeLookupState.NotFound(code)
                    else BarcodeLookupState.Failed(code, m ?: "Lookup failed", e)
                },
                onSuccess = { p -> _barcodeLookup.value = BarcodeLookupState.Found(code, p) }
            )
        }
    }

    fun clearBarcodeLookup() {
        barcodeJob?.cancel()
        _barcodeLookup.value = BarcodeLookupState.Idle
    }

    /** AccountBlocked screen "Log out": clears the session and the blocked flag. */
    fun acknowledgeAccountBlocked() {
        _accountBlocked.value = false
        logout()
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
        if (_createOrderState.value.isLoading) return
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

    fun getUserOrders(filter: OrderFilter = _orderFilter.value, force: Boolean = true) {
        val current = _userOrdersState.value
        if (!force && filter == _orderFilter.value && (current.isLoading || current.success != null)) return
        _orderFilter.value = filter
        orderRequest = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(filter, orderRequest).collectInto(
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
            repository.getMyOrders(_orderFilter.value, next).collectInto(
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
                onLoading = {
                    val keep = _orderDetailState.value.success?.takeIf { it.orderId == orderId }
                    _orderDetailState.value = GetOrderByIdState(isLoading = true, success = keep)
                },
                onError = { msg, _ ->
                    val keep = _orderDetailState.value.success?.takeIf { it.orderId == orderId }
                    _orderDetailState.value = GetOrderByIdState(error = msg, success = keep)
                },
                onSuccess = { _orderDetailState.value = GetOrderByIdState(success = it) }
            )
        }
    }

    /** History tab: GET orders?status=sold|returned (+ from/to), paged. */
    fun getHistoryOrders(filter: OrderFilter = _historyFilter.value, force: Boolean = true) {
        val current = _historyState.value
        if (!force && filter == _historyFilter.value && (current.isLoading || current.success != null)) return
        _historyFilter.value = filter
        historyRequest = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(filter, historyRequest).collectInto(
                onLoading = { _historyState.value = GetUsersOrdersState(isLoading = true) },
                onError = { msg, err -> _historyState.value = GetUsersOrdersState(error = msg, appError = err) },
                onSuccess = {
                    _historyState.value = GetUsersOrdersState(
                        success = it, pagination = it.pagination, endReached = it.pagination?.hasMore() != true
                    )
                }
            )
        }
    }

    fun loadMoreHistory() {
        val current = _historyState.value
        if (current.isLoading || current.isLoadingMore || current.endReached) return
        val next = historyRequest.next(current.pagination)
        if (next == null) {
            _historyState.value = current.copy(endReached = true)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMyOrders(_historyFilter.value, next).collectInto(
                onLoading = { _historyState.value = current.copy(isLoadingMore = true) },
                onError = { msg, err ->
                    _historyState.value = current.copy(isLoadingMore = false, error = msg, appError = err)
                },
                onSuccess = { page ->
                    historyRequest = next
                    _historyState.value = GetUsersOrdersState(
                        success = PagedData(current.success?.items.orEmpty() + page.items, page.pagination),
                        pagination = page.pagination,
                        endReached = page.pagination?.hasMore() != true
                    )
                }
            )
        }
    }


    /** PATCH orders/{id} (pending only): patient / doctor name. */
    fun updateOrderInfo(orderId: String, patientName: String?, doctorName: String?) {
        if (_editOrderState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrder(orderId, patientName, doctorName).collectInto(
                onLoading = { _editOrderState.value = EditOrderState(isLoading = true) },
                onError = { msg, err -> _editOrderState.value = EditOrderState(error = msg, appError = err) },
                onSuccess = {
                    _editOrderState.value = EditOrderState(success = it)
                    _orderDetailState.value = GetOrderByIdState(success = it)
                    getUserOrders(_orderFilter.value)
                }
            )
        }
    }

    fun resetEditOrderState() { _editOrderState.value = EditOrderState() }

    fun resetCancelOrderState() { _cancelOrderState.value = CancelOrderState() }

    fun resetUploadPrescriptionState() { _uploadPrescriptionState.value = UploadPrescriptionState() }

    fun cancelOrder(orderId: String, reason: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cancelOrder(orderId, reason?.takeIf { it.isNotBlank() }).collectInto(
                onLoading = { _cancelOrderState.value = CancelOrderState(isLoading = true) },
                onError = { msg, err -> _cancelOrderState.value = CancelOrderState(error = msg, appError = err) },
                onSuccess = {
                    _cancelOrderState.value = CancelOrderState(success = it)
                    _orderDetailState.value = GetOrderByIdState(success = it)
                    getUserOrders(_orderFilter.value)
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
                    getUserOrders(_orderFilter.value)
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
            resetAllState()
            setUserLoggedOut()
            Log.d("MyViewModel", "User logged out successfully")
        }
    }
}
