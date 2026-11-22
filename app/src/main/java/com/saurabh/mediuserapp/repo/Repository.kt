package com.saurabh.mediuserapp.repo

import android.content.Context
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.data.v1.api.AuthApi
import com.saurabh.mediuserapp.data.v1.api.DevicesApi
import com.saurabh.mediuserapp.data.v1.api.HealthApi
import com.saurabh.mediuserapp.data.v1.api.MetaApi
import com.saurabh.mediuserapp.data.v1.api.OrdersApi
import com.saurabh.mediuserapp.data.v1.api.ProductsApi
import com.saurabh.mediuserapp.data.v1.api.UsersApi
import com.saurabh.mediuserapp.data.v1.core.ApiResult
import com.saurabh.mediuserapp.data.v1.dto.GoogleLoginRequest
import com.saurabh.mediuserapp.ui.screens.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import com.saurabh.mediuserapp.data.v1.core.HealthResponse
import com.saurabh.mediuserapp.data.v1.core.PageRequest
import com.saurabh.mediuserapp.data.v1.core.map
import com.saurabh.mediuserapp.data.v1.core.safeApiCall
import com.saurabh.mediuserapp.data.v1.core.safeApiCallPaged
import com.saurabh.mediuserapp.data.v1.core.toApiError
import com.saurabh.mediuserapp.data.v1.core.toAppError
import com.saurabh.mediuserapp.data.v1.dto.AppConfig
import com.saurabh.mediuserapp.data.v1.dto.CheckoutCreateRequest
import com.saurabh.mediuserapp.data.v1.dto.CheckoutDto
import com.saurabh.mediuserapp.data.v1.dto.DeviceDto
import com.saurabh.mediuserapp.data.v1.dto.DeviceRegisterRequest
import com.saurabh.mediuserapp.data.v1.dto.InvoiceDto
import com.saurabh.mediuserapp.data.v1.dto.LoginData
import com.saurabh.mediuserapp.data.v1.dto.LoginRequest
import com.saurabh.mediuserapp.data.v1.dto.LogoutRequest
import com.saurabh.mediuserapp.data.v1.dto.OrderCancelRequest
import com.saurabh.mediuserapp.data.v1.dto.OrderUpdateRequest
import com.saurabh.mediuserapp.data.v1.dto.OrderCreateRequest
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetConfirmRequest
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetRequest
import com.saurabh.mediuserapp.data.v1.dto.PasswordResetRequestData
import com.saurabh.mediuserapp.data.v1.dto.ProductCategoryDto
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.data.v1.dto.ProfileUpdateRequest
import com.saurabh.mediuserapp.data.v1.dto.TokenData
import com.saurabh.mediuserapp.data.v1.dto.UserCreateRequest
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.dto.VerifyOtpRequest
import com.saurabh.mediuserapp.data.v1.dto.textParts
import com.saurabh.mediuserapp.network.ApiProvider
import com.saurabh.mediuserapp.network.TokenBundle
import com.saurabh.mediuserapp.network.TokenManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** CUSTOMER repository on the v1 API. Idempotency keys are always supplied by the caller. */
@Singleton
class Repository @Inject constructor(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val productsApi: ProductsApi,
    private val ordersApi: OrdersApi,
    private val devicesApi: DevicesApi,
    private val metaApi: MetaApi,
    private val healthApi: HealthApi,
    private val tokenManager: TokenManager,
    @ApplicationContext private val appContext: Context? = null
) {
    @Volatile
    private var appConfig: AppConfig? = null

    // ---------------- auth ----------------

    fun login(email: String, password: String): Flow<ResultState<LoginData>> = v1Flow {
        safeApiCall { authApi.login(LoginRequest(email, password)) }
    }

    /** Saves the session (both tokens, role, account id) on success; the role gate is the ViewModel's job. */
    fun verifyOtp(accountId: String, otp: String): Flow<ResultState<TokenData>> = v1Flow {
        safeApiCall { authApi.verifyOtp(VerifyOtpRequest(accountId, otp)) }.also { r ->
            if (r is ApiResult.Success) {
                val t = r.data
                tokenManager.saveSession(
                    TokenBundle(t.accessToken, t.refreshToken, t.role, t.accountId, t.accountType, t.expiresIn)
                )
            }
        }
    }

    /** POST auth/google: tokens come back directly (no OTP); the session is saved like [verifyOtp]. */
    fun loginWithGoogle(idToken: String): Flow<ResultState<TokenData>> = v1Flow {
        safeApiCall { authApi.googleLogin(GoogleLoginRequest(idToken)) }.also { r ->
            if (r is ApiResult.Success) {
                val t = r.data
                tokenManager.saveSession(
                    TokenBundle(t.accessToken, t.refreshToken, t.role, t.accountId, t.accountType, t.expiresIn)
                )
            }
        }
    }

    fun requestPasswordReset(email: String): Flow<ResultState<PasswordResetRequestData>> = v1Flow {
        safeApiCall { authApi.requestPasswordReset(PasswordResetRequest(email)) }
    }

    fun confirmPasswordReset(email: String, otp: String, newPassword: String): Flow<ResultState<Unit>> = v1Flow {
        safeApiCall { authApi.confirmPasswordReset(PasswordResetConfirmRequest(email, otp, newPassword)) }.map { }
    }

    /** Unregisters the stored device (failure ignored), revokes tokens server-side, ALWAYS clears local tokens. */
    fun logout(): Flow<ResultState<Unit>> = v1Flow {
        try {
            tokenManager.getDeviceId()?.let { safeApiCall { devicesApi.unregisterDevice(it) } }
            safeApiCall { authApi.logout(LogoutRequest(tokenManager.getRefreshToken())) }
        } finally {
            tokenManager.clearTokens()
            appContext?.let { GoogleCredentials.clear(it) }
        }
        ApiResult.Success(Unit)
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun currentRole(): String? = tokenManager.getRole()

    fun currentAccountId(): String? = tokenManager.getAccountId()

    fun clearSession() = tokenManager.clearTokens()

    // ---------------- profile ----------------

    fun signup(request: UserCreateRequest, image: ImagePart? = null): Flow<ResultState<UserDto>> = v1Flow {
        safeApiCall { usersApi.createUser(request.toParts(), image?.toPart("image")) }
    }

    fun getMe(): Flow<ResultState<UserDto>> = v1Flow { safeApiCall { usersApi.getMe() } }

    fun updateMe(request: ProfileUpdateRequest, image: ImagePart? = null): Flow<ResultState<UserDto>> = v1Flow {
        safeApiCall { usersApi.updateMe(request.toParts(), image?.toPart("image")) }
    }

    // ---------------- catalog ----------------

    fun getProducts(
        filter: ProductFilter = ProductFilter(),
        page: PageRequest = PageRequest()
    ): Flow<ResultState<PagedData<ProductDto>>> = v1Paged {
        val q = filter.toQuery()
        safeApiCallPaged {
            productsApi.getProducts(
                q = q["q"], category = q["category"], scheduleType = q["schedule_type"],
                requiresPrescription = filter.requiresPrescription, inStock = filter.inStock,
                updatedSince = q["updated_since"], page = page.toQueryMap()
            )
        }
    }

    fun getProduct(productId: String): Flow<ResultState<ProductDto>> = v1Flow {
        safeApiCall { productsApi.getProduct(productId) }
    }

    fun getProductByBarcode(barcode: String): Flow<ResultState<ProductDto>> = v1Flow {
        safeApiCall { productsApi.getProductByBarcode(barcode) }
    }

    fun getProductCategories(page: PageRequest = PageRequest()): Flow<ResultState<PagedData<ProductCategoryDto>>> =
        v1Paged { safeApiCallPaged { productsApi.getCategories(page.toQueryMap()) } }

    // ---------------- orders ----------------

    fun createOrder(
        request: OrderCreateRequest,
        prescription: FilePart?,
        idempotencyKey: String
    ): Flow<ResultState<OrderDto>> = v1Flow {
        safeApiCall { ordersApi.createOrder(request.toParts(), prescription?.toPart("prescription"), idempotencyKey) }
    }

    fun getMyOrders(
        filter: OrderFilter = OrderFilter(),
        page: PageRequest = PageRequest()
    ): Flow<ResultState<PagedData<OrderDto>>> = v1Paged {
        val q = filter.toQuery()
        safeApiCallPaged {
            ordersApi.getOrders(
                status = q["status"], from = q["from"], to = q["to"],
                updatedSince = q["updated_since"], checkoutId = q["checkout_id"], page = page.toQueryMap()
            )
        }
    }

    fun getOrder(orderId: String): Flow<ResultState<OrderDto>> = v1Flow {
        safeApiCall { ordersApi.getOrder(orderId) }
    }

    /** PATCH orders/{id}: own pending orders, patient_name / doctor_name only. */
    fun updateOrder(orderId: String, patientName: String?, doctorName: String?): Flow<ResultState<OrderDto>> = v1Flow {
        safeApiCall { ordersApi.updateOrder(orderId, OrderUpdateRequest(patientName, doctorName)) }
    }

    fun cancelOrder(orderId: String, reason: String? = null): Flow<ResultState<OrderDto>> = v1Flow {
        safeApiCall { ordersApi.cancelOrder(orderId, OrderCancelRequest(reason)) }
    }

    fun getInvoice(orderId: String): Flow<ResultState<InvoiceDto>> = v1Flow {
        safeApiCall { ordersApi.getInvoice(orderId) }
    }

    /** POST checkouts (JSON). Idempotency key is supplied by the caller. */
    fun createCheckout(request: CheckoutCreateRequest, idempotencyKey: String): Flow<ResultState<CheckoutDto>> = v1Flow {
        safeApiCall { ordersApi.createCheckout(request, idempotencyKey) }
    }

    /** GET checkouts/{checkout_id}/invoice (combined). */
    fun getCheckoutInvoice(checkoutId: String): Flow<ResultState<InvoiceDto>> = v1Flow {
        safeApiCall { ordersApi.getCheckoutInvoice(checkoutId) }
    }

    fun uploadPrescription(
        orderId: String,
        file: FilePart,
        doctorName: String? = null,
        patientName: String? = null
    ): Flow<ResultState<OrderDto>> = v1Flow {
        safeApiCall {
            ordersApi.uploadPrescription(
                orderId, file.toPart("prescription"),
                textParts("doctor_name" to doctorName, "patient_name" to patientName)
            )
        }
    }

    // ---------------- devices / meta ----------------

    /** Stores the returned device id for logout. */
    fun registerDevice(request: DeviceRegisterRequest): Flow<ResultState<DeviceDto>> = v1Flow {
        safeApiCall { devicesApi.registerDevice(request) }.also { r ->
            if (r is ApiResult.Success) tokenManager.saveDeviceId(r.data.deviceId)
        }
    }

    fun unregisterDevice(deviceId: String): Flow<ResultState<DeviceDto>> = v1Flow {
        safeApiCall { devicesApi.unregisterDevice(deviceId) }.also { r ->
            if (r is ApiResult.Success && tokenManager.getDeviceId() == deviceId) tokenManager.saveDeviceId(null)
        }
    }

    /** Fetches and caches the config (includes serverTime and min_app_version). */
    fun getAppConfig(): Flow<ResultState<AppConfig>> = v1Flow {
        safeApiCall { metaApi.getAppConfig() }.also { r -> if (r is ApiResult.Success) appConfig = r.data }
    }

    fun cachedAppConfig(): AppConfig? = appConfig

    /** ready = false -> /health, true -> /health/ready. */
    fun health(ready: Boolean = false): Flow<ResultState<HealthResponse>> = v1Flow {
        val url = ApiProvider.BASE_URL1.trimEnd('/') + "/" + if (ready) HealthApi.READY else HealthApi.LIVE
        try {
            val r = healthApi.getHealth(url)
            val body = r.body()
            if (r.isSuccessful && body != null) ApiResult.Success(body) else ApiResult.Failure(r.toApiError())
        } catch (e: Throwable) {
            ApiResult.Failure(e.toAppError())
        }
    }
}
