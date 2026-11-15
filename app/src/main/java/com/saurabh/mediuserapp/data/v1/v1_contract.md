# v1 contract (CUSTOMER app) — Part 1

Package root: `com.saurabh.mediuserapp`. Source of truth: `/response.md` (repo root). Paths below are relative to `app/src/main/java/com/saurabh/mediuserapp/`.

## UPDATED existing classes

- `TokenManager` — network/TokenManager.kt — now `internal constructor(prefs: SharedPreferences, clock: () -> Long)`; `getInstance(context)` unchanged (EncryptedSharedPreferences `auth_prefs_secure`, one-time migration from plain `auth_prefs`, falls back to plain on keystore failure)
  - NEW `val refreshLock: ReentrantLock` — the ONE refresh lock (old + v1)
  - NEW `fun refreshSingleFlight(requestAccessToken: String?, refresher: (refreshToken: String) -> TokenBundle?): String?` — the ONE refresh function
  - NEW `fun saveSession(bundle: TokenBundle)` — saves access + refresh + role + account_id (also user_id) + account_type + expires_at
  - NEW `fun getRole(): String?`, `fun getAccountId(): String?` (falls back to user_id), `fun getAccountType(): String?`, `fun getExpiresAt(): Long?` (epoch ms), `fun isAccessTokenExpired(): Boolean`, `fun clear(): Boolean` (no event)
  - KEPT `saveTokens`, `updateTokens`, `getAccessToken`, `getRefreshToken`, `getUserRole`, `getUserId`, `saveUserId`, `saveRole`, `updateAccessToken`, `clearTokens`, `isLoggedIn()`, `invalidateSession()`, `sessionExpiredEvent`
- `TokenBundle(accessToken, refreshToken, role?, accountId?, accountType?, expiresInSeconds?)` — network/TokenManager.kt
- `TokenAuthenticator(tokenManager, apiServiceProvider: Lazy<ApiServices>, v1AuthApi: Lazy<AuthApi>, appEvents: AppEvents)` — network/TokenAuthenticator.kt
  - v1 (path contains `/api/v1/`): 401 + `TOKEN_EXPIRED` -> single-flight `POST auth/refresh` (Bearer REFRESH), save both tokens + role + account_id, retry once. Other 401 code, refresh 401/403/failure, or 2nd attempt -> `invalidateSession()` + `AppEvent.SessionInvalid`, return null. Public paths and auth/refresh never refresh.
  - legacy: same behaviour as before, now via `refreshSingleFlight`
- `AuthInterceptor` — network/AuthInterceptor.kt — v1/health branch: Bearer access token except auth/login, auth/verify-otp, auth/password-reset/..., app-config, openapi.json, `POST users`, /health*; auth/refresh gets the REFRESH token; never overwrites an explicit Authorization header. Legacy branch unchanged.
- `ApiProvider` — network/ApiProvider.kt
  - `@V1Retrofit` qualifier + `provideV1Retrofit(okHttpClient): Retrofit` — shared client `.newBuilder()`, base `V1_BASE_URL`, `V1Gson`
  - `@V1AuthRetrofit` qualifier + `provideV1AuthRetrofit(okHttpClient): Retrofit` — same, `Authenticator.NONE`
  - `provideV1AuthApi(@V1AuthRetrofit): AuthApi`
  - `val V1_BASE_URL = BASE_URL1.trimEnd('/') + "/api/v1/"`; v1 connect timeout 15 s debug / 90 s release; read/write 90 s (inherited)
  - main client now also has `VersionInterceptor`, `ErrorEventInterceptor` (v1 only); HttpLoggingInterceptor redacts `Authorization` header + secrets in bodies (all paths)
- `LoggingInterceptor` — network/LoggingInterceptor.kt — `companion fun redact(text: String?): String` (access_token, refresh_token, token, otp, password, new_password, Bearer); Authorization header masked

## NEW

### network
- `V1Paths` (internal object) — network/V1Paths.kt — `isV1(Request)`, `isV1Path(String)`, `isHealth(String)`, `relative(String)`, `isRefresh(Request)`, `isPublic(Request)`
- `VersionInterceptor @Inject (appEvents)` — network/VersionInterceptor.kt — v1 only: header `X-App-Version = BuildConfig.VERSION_NAME`; 426 -> `AppEvent.UpgradeRequired(X-Min-App-Version)`
- `ErrorEventInterceptor @Inject (appEvents)` — network/ErrorEventInterceptor.kt — v1 only, peeks body: 403 ACCOUNT_BLOCKED -> `AccountBlocked`, 401 TOKEN_INVALID -> `SessionInvalid`, 429 -> `RateLimited`

### data/v1/core
- `ApiEnvelope<T>(status: Int, message: String?, data: T?)` — core/ApiEnvelope.kt
- `PagedEnvelope<T>(status: Int, message: String?, data: List<T>?, pagination: Pagination?, summary: JsonObject?)` — core/ApiEnvelope.kt (`summary` // unverified)
- `Pagination(limit: Int, offset: Int, total: Int)` — core/ApiEnvelope.kt
- `HealthResponse(service: String?, status: String?, checks: Map<String,String>?)` — core/ApiEnvelope.kt (not enveloped)
- `sealed class ApiResult<out T>`: `Success(data: T, pagination: Pagination? = null, message: String? = null)`, `Failure(error: AppError)` — core/ApiResult.kt
- `ApiResult<T>.map(transform)` — core/ApiResult.kt
- `suspend fun <T> safeApiCall(block: suspend () -> Response<ApiEnvelope<T>>): ApiResult<T>` — core/ApiResult.kt (null data / 204 -> Success(null): declare T nullable)
- `suspend fun <T> safeApiCallPaged(block: suspend () -> Response<PagedEnvelope<T>>): ApiResult<List<T>>` — core/ApiResult.kt
- `Response<*>.toApiError(): AppError.Api` (missing code -> `ErrorCode.fromHttpStatus`) — core/ApiResult.kt
- `Throwable.toAppError(): AppError` (SocketTimeout/timeout -> Timeout, IOException -> Network, Json/EOF -> Unknown, rethrows CancellationException) — core/ApiResult.kt
- `PageRequest(limit = 20, offset = 0, sort: String? = null, order: String? = null)`: `toQueryMap(): Map<String,String>`, `next(pagination: Pagination?): PageRequest?`, `DEFAULT_LIMIT = 20` — core/PageRequest.kt
- `Pagination.hasMore(): Boolean` — core/PageRequest.kt
- `suspend fun <T> loadMore(current: List<T>, lastRequest: PageRequest, lastPagination: Pagination?, fetch: suspend (PageRequest) -> ApiResult<List<T>>): ApiResult<List<T>>` — core/PageRequest.kt
- `sealed interface UiState<out T>`: `Loading`, `Success(data)`, `Empty`, `Error(error: AppError)`; `ApiResult<T>.toUiState()` — core/UiState.kt
- `enum OrderStatus(wire)`: PENDING("pending"), APPROVED("approved"), SOLD("sold"), CANCELLED("cancelled"), RETURNED("returned"), UNKNOWN("unknown"); `from(String?)` — core/OrderStatus.kt
- `object V1Gson`: `instance: Gson`, `create()`, `parseInstant(String?)`, `parseLocalDate(String?)` — core/V1Gson.kt (registered ONLY on v1 Retrofits)
- `InstantAdapter` (ISO `...Z`, offset, local, date-only `YYYY-MM-DD`; blank/invalid -> null), `LocalDateAdapter`, `OrderStatusAdapter`, `ErrorCodeAdapter` — core/V1Gson.kt
- `sealed interface AppEvent`: `UpgradeRequired(minVersion: String?)`, `SessionInvalid`, `AccountBlocked`, `RateLimited` — core/AppEvents.kt
- `@Singleton class AppEvents @Inject`: `events: SharedFlow<AppEvent>`, `emit(event): Boolean` — core/AppEvents.kt
- `@Singleton class IdempotencyKeyProvider @Inject`: `newKey(): String` (UUID), `HEADER = "Idempotency-Key"` — core/IdempotencyKeyProvider.kt

### data/v1/error
- `enum ErrorCode`: VALIDATION_ERROR, PRESCRIPTION_REQUIRED, UNAUTHORIZED, TOKEN_EXPIRED, TOKEN_INVALID, FORBIDDEN, ACCOUNT_BLOCKED, ACCOUNT_NOT_APPROVED, NOT_FOUND, METHOD_NOT_ALLOWED, CONFLICT, OUT_OF_STOCK, PRESCRIPTION_NOT_VERIFIED, INVALID_TRANSITION, ALREADY_SOLD, BATCH_EXPIRED, PAYLOAD_TOO_LARGE, UNSUPPORTED_MEDIA_TYPE, UPGRADE_REQUIRED, RATE_LIMITED, INTERNAL_ERROR, UNKNOWN; `from(String?)`, `fromHttpStatus(Int)` — error/ErrorCode.kt
- `ErrorBody(status: Int?, message: String?, code: String?, fields: Map<String,String>?)`; `ErrorBody.parse(raw: String?): ErrorBody?` — error/ErrorBody.kt
- `sealed class AppError`: `Api(httpStatus: Int, code: ErrorCode, message: String?, fields: Map<String,String>?)`, `Network`, `Timeout`, `Unknown(cause: Throwable?)`; `userMessage(): String` — error/AppError.kt

### data/v1/dto (AuthDtos.kt)
- `ACCOUNT_TYPE_USER = "user"`
- `LoginRequest(email, password, accountType = "user")`; `LoginData(accountId, accountType, otpExpiresIn: Int, otpRequired: Boolean)`
- `VerifyOtpRequest(accountId, otp, accountType = "user")`
- `TokenData(accessToken, refreshToken, tokenType, expiresIn: Long, role, accountType, accountId)`; `typealias RefreshData = TokenData`
- `LogoutRequest(refreshToken: String?)`; `LogoutData(revoked: List<String>)`
- `PasswordResetRequest(email, accountType = "user")`; `PasswordResetRequestData(otpExpiresIn: Int)`
- `PasswordResetConfirmRequest(email, otp, newPassword, accountType = "user")` -> data null

### data/v1/api (AuthApi.kt, @V1AuthRetrofit)
- `login(LoginRequest): Response<ApiEnvelope<LoginData>>` — POST auth/login
- `verifyOtp(VerifyOtpRequest): Response<ApiEnvelope<TokenData>>` — POST auth/verify-otp
- `refreshCall(bearerRefreshToken): Call<ApiEnvelope<RefreshData>>` — POST auth/refresh (authenticator only)
- `logout(LogoutRequest): Response<ApiEnvelope<LogoutData>>` — POST auth/logout
- `requestPasswordReset(PasswordResetRequest): Response<ApiEnvelope<PasswordResetRequestData>>` — POST auth/password-reset/request
- `confirmPasswordReset(PasswordResetConfirmRequest): Response<ApiEnvelope<Any?>>` — POST auth/password-reset/confirm

### Build / tests
- `app/build.gradle.kts`: `testImplementation(libs.mockwebserver)`, `testOptions.unitTests.isReturnDefaultValues = true`; `libs.versions.toml`: `mockwebserver` (okhttp version)
- Tests: test/.../data/v1/SafeApiCallTest.kt, test/.../data/v1/V1GsonTest.kt, test/.../network/TokenAuthenticatorTest.kt

### Notes
- Not yet created (later parts): users/products/orders/devices/app-config APIs and DTOs; customer-only per response.md.
- `// unverified`: `PagedEnvelope.summary` shape.

---

# PART 2 — DTOs + APIs (customer)

Paths relative to `data/v1/`. All responses `Response<ApiEnvelope<T>>` / `Response<PagedEnvelope<T>>` unless noted. Money Double, timestamps Instant, dates LocalDate, ids String.

## DTOs (17 response/request classes)
- `UserDto` — dto/UserDtos.kt — user_id, name, email, role, phone_number?, address?, pin_code?, image_url?, is_approved, is_blocked, is_deleted, created_at
- `UserCreateRequest(name, email, password, phoneNumber?, address?, pinCode?)` — dto/UserDtos.kt — `toParts(): Map<String, RequestBody>` (multipart text parts)
- `ProfileUpdateRequest(name?, address?, email?, phoneNumber?, pinCode?, password?, currentPassword?)` — dto/UserDtos.kt — `toParts()`; current_password required for email/password change
- `DeviceDto` — dto/DeviceDtos.kt — device_id, user_id, platform, app_version?, is_active, created_at, updated_at
- `DeviceRegisterRequest(token, platform = "android", appVersion?)` — dto/DeviceDtos.kt (JSON)
- `ProductDto` — dto/ProductDtos.kt — product_id, name, generic_name, brand_name, manufacturer, category, dosage_form, strength, pack_size, unit, hsn_code, gst_percent, price, mrp, purchase_price? (null for customers), schedule_type, requires_prescription, storage_conditions, reorder_level:Int, barcode, image_url?, is_active, stock:Int, sellable_stock:Int, in_stock, created_at, updated_at
- `ProductCategoryDto(name, productCount)` — dto/ProductDtos.kt
- `OrderDto` — dto/OrderDtos.kt — order_id, user_id, user_name, product_id, product_name, category, image_url?, quantity:Int, price, total_amount, status:OrderStatus, message, doctor_name, patient_name, prescription_url?, prescription_verified, requires_prescription, cancel_reason, created_at, updated_at, sales:List<JsonObject>? (GET order only), restocked:List<JsonObject>? (cancel only)
- `OrderCreateRequest(productId, quantity, message?, doctorName?, patientName?, prescriptionUrl?)` — dto/OrderDtos.kt — `toParts()`
- `OrderUpdateRequest(patientName?, doctorName?)` — dto/OrderDtos.kt (JSON)
- `OrderCancelRequest(cancelReason?)` — dto/OrderDtos.kt (JSON)
- `InvoiceDto` — dto/InvoiceDtos.kt — invoice_number, currency, issued_at, generated_at, prices_include_gst, subtotal, gst_total, grand_total, items:List<InvoiceItemDto>, order:OrderDto, user:InvoiceUserDto
- `InvoiceItemDto` — dto/InvoiceDtos.kt — product_id, product_name, batch_id?, batch_no?, expiry_date:LocalDate?, quantity, unit_price, taxable_amount, gst_percent, gst_amount, line_total
- `InvoiceUserDto` — dto/InvoiceDtos.kt — user_id, name, email, phone_number?, address?, pin_code?
- `ApiIndex(name, version, apiVersion, links:List<ApiLink>)`, `ApiLink(rel, href, methods)` — dto/MetaDtos.kt
- `AppConfig` — dto/MetaDtos.kt — api_prefix, api_version, docs_enabled, min_app_version?, server_time, enums:AppConfigEnums, error_codes:List<String>, otp:OtpConfig, pagination:PaginationConfig, uploads:UploadConfig
- `AppConfigEnums` (account_types, device_platforms, dosage_forms, order_statuses, prescription_schedules, schedule_types, stock_movement_reasons, units), `OtpConfig(expiresIn, length, maxAttempts)`, `PaginationConfig(defaultLimit, maxLimit)`, `UploadConfig(allowedMimeTypes, imageMimeTypes, maxRequestBytes:Long, maxUploadMb)` — dto/MetaDtos.kt
- (Part 1) auth DTOs unchanged in dto/AuthDtos.kt

## API functions (relative paths; `// page` = `PageRequest.toQueryMap()`)
- `AuthApi` (Part 1, @V1AuthRetrofit): login, verifyOtp, refreshCall, logout, requestPasswordReset, confirmPasswordReset — unchanged
- `UsersApi` (api/UsersApi.kt): `createUser(fields: Map<String,RequestBody>, image: MultipartBody.Part? = null): ApiEnvelope<UserDto>` POST users (public, multipart); `getMe(): ApiEnvelope<UserDto>` GET users/me; `updateMe(fields, image: MultipartBody.Part? = null): ApiEnvelope<UserDto>` PATCH users/me (multipart)
- `ProductsApi` (api/ProductsApi.kt): `getProducts(q?, category?, scheduleType?, requiresPrescription?, inStock?, updatedSince: String?, page: Map<String,String>): PagedEnvelope<ProductDto>` (sort name|price|mrp|stock|created_at|updated_at); `getProduct(productId): ApiEnvelope<ProductDto>`; `getProductByBarcode(barcode): ApiEnvelope<ProductDto>`; `getCategories(page): PagedEnvelope<ProductCategoryDto>` (sort name|product_count)
- `OrdersApi` (api/OrdersApi.kt): `createOrder(fields, prescription: MultipartBody.Part? = null, idempotencyKey: String? = null): ApiEnvelope<OrderDto>` POST orders (multipart, `Idempotency-Key`); `getOrders(status?, productId?, from?, to?, updatedSince?, page): PagedEnvelope<OrderDto>`; `getOrder(orderId)`; `updateOrder(orderId, OrderUpdateRequest)` PATCH (own pending: patient/doctor name); `cancelOrder(orderId, OrderCancelRequest = OrderCancelRequest())`; `getInvoice(orderId): ApiEnvelope<InvoiceDto>` (409 INVALID_TRANSITION unless sold/returned); `uploadPrescription(orderId, prescription: MultipartBody.Part, fields = emptyMap()): ApiEnvelope<OrderDto>` (multipart)
- `DevicesApi` (api/DevicesApi.kt): `registerDevice(DeviceRegisterRequest): ApiEnvelope<DeviceDto>`; `unregisterDevice(deviceId): ApiEnvelope<DeviceDto>`
- `MetaApi` (api/MetaApi.kt): `getIndex(): ApiEnvelope<ApiIndex>` (GET "."); `getAppConfig(): ApiEnvelope<AppConfig>`; `getOpenApi(): Response<JsonObject>` (not enveloped)
- `HealthApi` (api/MetaApi.kt): `getHealth(@Url url: String): Response<HealthResponse>`; consts `LIVE = "health"`, `READY = "health/ready"` (append to server root, outside api/v1)
- Hilt: `V1ApiModule` (di/V1ApiModule.kt) provides UsersApi, ProductsApi, OrdersApi, DevicesApi, MetaApi, HealthApi on `@V1Retrofit`; AuthApi stays in ApiProvider.

## `// unverified` fields
- `OrderDto.sales`, `OrderDto.restocked` (element shape undocumented; `[]` in examples; present only on GET order / cancel)
- `DeviceDto.appVersion` (optional on request, nullability of response not shown)
- `InvoiceItemDto.batchId`, `.batchNo`, `.expiryDate` (non-null in example; nullability not stated)
- `PagedEnvelope.summary` (Part 1)

## Skipped endpoints
- Admin-only: GET users, users/{id} PATCH/DELETE/approve/block/unblock, all admins/*, products POST/PATCH/deactivate/reactivate, batches/*, products/{id}/batches, stock/*, stock-movements, inventory/*, orders approve/reject/return/sell/verify-prescription, sales, suppliers/*, dashboard/summary, reports/*, audit-logs
- Redundant for customers: GET users/{user_id} (use users/me), GET users/{user_id}/orders (use GET orders, already own-only)
- Docs-only: GET /api/docs, GET /dox (HTML, not API)
