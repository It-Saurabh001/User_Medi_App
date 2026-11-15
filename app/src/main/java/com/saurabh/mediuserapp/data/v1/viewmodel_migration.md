CUSTOMER APP ONLY

# ViewModel migration handoff (Repository on API v1)

Package `com.saurabh.mediuserapp`. Repository: `repo/Repository.kt` (`@Singleton`, `@Inject`). Helpers: `repo/RepositorySupport.kt`. All methods are NON-suspend, return cold `Flow<ResultState<T>>` (Loading, then Success/Error). `T` = unwrapped `data`. Lists = `PagedData<T>`.

## 1. Repository methods
| method | returns | status | old name | idem key |
|---|---|---|---|---|
| `login(email, password)` | `LoginData` | REWRITTEN | loginUser | N |
| `verifyOtp(accountId, otp)` | `TokenData` (session saved inside) | REWRITTEN | verifyUserOtp | N |
| `requestPasswordReset(email)` | `PasswordResetRequestData` | REWRITTEN | requestUserPasswordReset | N |
| `confirmPasswordReset(email, otp, newPassword)` | `Unit` | REWRITTEN | resetUserPasswordWithOtp (userId -> email) | N |
| `logout()` | `Unit` (always Success; tokens always cleared; collect it!) | REWRITTEN | logout (was plain fun) | N |
| `isLoggedIn()` | `Boolean` | REWRITTEN | isUserLoggedIn | N |
| `currentRole()` | `String?` | REWRITTEN | getCurrentUserRole | N |
| `currentAccountId()` | `String?` | REWRITTEN | getCurrentUserId | N |
| `clearSession()` | `Unit` (local only, no API) | NEW | - | N |
| `signup(request: UserCreateRequest, image: ImagePart? = null)` | `UserDto` | REWRITTEN | createUser | N |
| `getMe()` | `UserDto` | REWRITTEN | getSpecificUser(userId) | N |
| `updateMe(request: ProfileUpdateRequest, image: ImagePart? = null)` | `UserDto` | NEW | - | N |
| `getProducts(filter = ProductFilter(), page = PageRequest())` | `PagedData<ProductDto>` | REWRITTEN | getAllProducts | N |
| `getProduct(productId)` | `ProductDto` | REWRITTEN | getSpecificProduct | N |
| `getProductByBarcode(barcode)` | `ProductDto` | NEW | - | N |
| `getProductCategories(page = PageRequest())` | `PagedData<ProductCategoryDto>` | NEW | - | N |
| `createOrder(request: OrderCreateRequest, prescription: FilePart?, idempotencyKey: String)` | `OrderDto` | REWRITTEN | createOrder | **Y (caller supplies)** |
| `getMyOrders(filter = OrderFilter(), page = PageRequest())` | `PagedData<OrderDto>` | REWRITTEN | getUserOrders(userId) | N |
| `getOrder(orderId)` | `OrderDto` (has `sales`) | REWRITTEN | getOrderById | N |
| `cancelOrder(orderId, reason: String? = null)` | `OrderDto` | NEW | - | N |
| `getInvoice(orderId)` | `InvoiceDto` (409 INVALID_TRANSITION unless sold/returned) | NEW | - | N |
| `uploadPrescription(orderId, file: FilePart, doctorName? = null, patientName? = null)` | `OrderDto` | NEW | - | N |
| `registerDevice(request: DeviceRegisterRequest)` | `DeviceDto` (device id stored) | NEW | - | N |
| `unregisterDevice(deviceId)` | `DeviceDto` | NEW | - | N |
| `getAppConfig()` | `AppConfig` (cached in memory) | NEW | - | N |
| `cachedAppConfig()` | `AppConfig?` (sync) | NEW | - | N |
| `health(ready: Boolean = false)` | `HealthResponse` | NEW | - | N |
REMOVED (no customer endpoint): `getSellHistoryByUserId` (use `getMyOrders(OrderFilter(status = OrderStatus.SOLD))`), `getStoredToken` (tokens are internal; interceptors attach them).

## 2. Type reference (`?` = nullable)
- `LoginData(accountId:String, accountType:String, otpExpiresIn:Int, otpRequired:Boolean)`
- `TokenData(accessToken, refreshToken, tokenType:String, expiresIn:Long, role:String, accountType:String, accountId:String)`
- `PasswordResetRequestData(otpExpiresIn:Int)`
- `UserDto(userId, name, email, role:String, phoneNumber?, address?, pinCode?, imageUrl?:String, isApproved, isBlocked, isDeleted:Boolean, createdAt:Instant)`
- `UserCreateRequest(name, email, password, phoneNumber?, address?, pinCode?)`
- `ProfileUpdateRequest(name?, address?, email?, phoneNumber?, pinCode?, password?, currentPassword?)` (currentPassword required when email/password changes)
- `ProductDto(productId, name, genericName, brandName, manufacturer, category, dosageForm, strength, packSize, unit, hsnCode:String, gstPercent, price, mrp:Double, purchasePrice?:Double (always null), scheduleType:String, requiresPrescription:Boolean, storageConditions:String, reorderLevel:Int, barcode:String, imageUrl?:String, isActive:Boolean, stock:Int, sellableStock:Int, inStock:Boolean, createdAt, updatedAt:Instant)`
- `ProductCategoryDto(name:String, productCount:Int)`
- `OrderDto(orderId, userId, userName, productId, productName, category:String, imageUrl?:String, quantity:Int, price:Double, totalAmount:Double, status:OrderStatus, message, doctorName, patientName:String, prescriptionUrl?:String, prescriptionVerified, requiresPrescription:Boolean, cancelReason:String, createdAt, updatedAt:Instant, sales?:List<JsonObject>, restocked?:List<JsonObject>)`
- `OrderStatus`: PENDING, APPROVED, SOLD, CANCELLED, RETURNED, UNKNOWN (`.wire` = server string)
- `OrderCreateRequest(productId:String, quantity:Int, message?, doctorName?, patientName?, prescriptionUrl?:String)`
- `InvoiceDto(invoiceNumber, currency:String, issuedAt, generatedAt:Instant, pricesIncludeGst:Boolean, subtotal, gstTotal, grandTotal:Double, items:List<InvoiceItemDto>, order:OrderDto, user:InvoiceUserDto)`; `InvoiceItemDto(productId, productName, batchId?, batchNo?:String, expiryDate?:LocalDate, quantity:Int, unitPrice, taxableAmount, gstPercent, gstAmount, lineTotal:Double)`; `InvoiceUserDto(userId, name, email, phoneNumber?, address?, pinCode?)`
- `DeviceRegisterRequest(token:String, platform:String = "android", appVersion?:String)`; `DeviceDto(deviceId, userId, platform:String, appVersion?:String, isActive:Boolean, createdAt, updatedAt:Instant)`
- `AppConfig(apiPrefix, apiVersion:String, docsEnabled:Boolean, minAppVersion?:String, serverTime:Instant, enums:AppConfigEnums, errorCodes:List<String>, otp:OtpConfig(expiresIn, length, maxAttempts:Int), pagination:PaginationConfig(defaultLimit, maxLimit:Int), uploads:UploadConfig(allowedMimeTypes, imageMimeTypes:List<String>, maxRequestBytes:Long, maxUploadMb:Int))`
- `HealthResponse(service?:String, status?:String, checks?:Map<String,String>)`
- `PagedData<T>(items:List<T>, pagination?:Pagination)`; `Pagination(limit, offset, total:Int)`; `PageRequest(limit=20, offset=0, sort?, order?)`
- `ProductFilter(q?, category?, scheduleType?:String, requiresPrescription?, inStock?:Boolean, updatedSince?:Instant)`; `OrderFilter(status?:OrderStatus, dateRange?:DateRange, updatedSince?:Instant)`; `DateRange(from?, to?:LocalDate)`
- `FilePart(fileName:String, mimeType:String, bytes:ByteArray)`; `ImagePart` = typealias of `FilePart`
- `AppException(error: AppError)` extends Exception

## 3. Old -> new call map (`viewModel/MyViewModel.kt`)
- `createUser(name, password, phoneNumber, email, pinCode, address)` -> `signup(UserCreateRequest(name, email, password, phoneNumber, address, pinCode), image = null)`. Param `role` is dropped (server forces "user"). `CreateUserState.success` is now `UserDto` (was `CreateUserResponse`).
- `loginUser(email, password)` -> `login(email, password)`. `LoginUserResponse.user_id` -> `LoginData.accountId` (pass to verifyOtp); `.role` is gone (role comes from verifyOtp); `.message/.status` are gone.
- `verifyUserOtp(userId, otp)` -> `verifyOtp(accountId = userId, otp)`. Result `TokenData`: `access_token/refresh_token/role` -> `accessToken/refreshToken/role`. DELETE the VM's own `tokenManager.saveTokens(...)` (Repository already saved session incl. account id); keep `setUserLoggedIn()` after the role gate.
- `requestUserPasswordReset(email)` -> `requestPasswordReset(email)`. Result is only `otpExpiresIn`; there is NO `user_id` any more, so navigation to ForgotPasswordOtp/ResetPassword must carry the EMAIL instead of userId.
- `resetUserPasswordWithOtp(userId, otp, newPassword)` -> `confirmPasswordReset(email, otp, newPassword)`. Success data is `Unit`; show a "log in again" message.
- `getSpecificUser(userId)` -> `getMe()` (no params). `User.id` removed; `date_of_account_creation` -> `createdAt:Instant`; `isApproved` -> `isApproved`; `block` -> `isBlocked`; `_isApproved/_block/password` removed; new `isDeleted`, `imageUrl`.
- `getAllProducts()` -> `getProducts(ProductFilter(...), PageRequest())`. `GetAllProductResponse.products` -> `PagedData.items`; `ProductItem.Product_id` -> `productId`; `imageUrl` same; `id` removed; new fields (see type reference).
- `getSpecificProduct(productId)` -> `getProduct(productId)`; result `ProductDto` (was `GetSpecificProductResponse`).
- `createOrder(userId, productId, quantity: String, message)` -> `createOrder(OrderCreateRequest(productId, quantity.toInt(), message), prescription = null, idempotencyKey)`. `userId` param removed (server uses token). `CreateOrderState.success` is now `OrderDto` (was message-only). After success call `getMyOrders()` instead of `getUserOrders(userId)`.
- `getUserOrders(userId)` -> `getMyOrders(OrderFilter(), PageRequest())`. `GetUsersOrdersResponse.order` -> `PagedData.items`. `Order.date_of_order_creation` -> `createdAt:Instant`; `Order.sold` -> `status == SOLD`; `Order.isApproved` -> `status in (APPROVED, SOLD)`; `order_id/product_id/product_name/total_amount/user_id/user_name` -> camelCase; `id` removed.
- `getOrderById(orderId)` -> `getOrder(orderId)` (`OrderDto`).
- `getSellHistoryByUserId(userId)` -> removed. Use `getMyOrders(OrderFilter(status = OrderStatus.SOLD))` and `getInvoice(orderId)` for details; `SellHistory` fields have no v1 equivalent (`remaining_stock` is not exposed to customers).
- `getStoredToken()` -> removed. `getCurrentUserId()` -> `currentAccountId()`. `getCurrentUserRole()` -> `currentRole()`.
- `logout()` -> `repository.logout().collect { }` (it is a Flow now; calling it without collecting does nothing). Then `setUserLoggedOut()`. For an instant local logout (e.g. SessionInvalid) use `clearSession()`.
- WaitingScreen (`getSpecificUser` polling): unapproved accounts cannot log in on v1 (login returns ACCOUNT_NOT_APPROVED), so there is no token to poll with. Replace the waiting flow with a message after signup; keep the screen only if a design needs it.

## 4. State rules
- Map `ResultState` to `UiState`: Loading -> `UiState.Loading`; Success -> `UiState.Success(data)` (empty list -> `UiState.Empty`); Error -> `UiState.Error((e.exception as AppException).error)`. `UiState` lives in `data/v1/core`.
- Error: `(result.exception as? AppException)?.error` is an `AppError`: `Api(httpStatus, code: ErrorCode, message, fields)`, `Network`, `Timeout`, `Unknown(cause)`. Show `error.userMessage()`; use `fields` for per-input messages.
- ErrorCode branches: `OUT_OF_STOCK` -> show `fields["quantity"]` ("available: N"), let user lower quantity; `PRESCRIPTION_REQUIRED` -> ask for a prescription file + doctor name (`fields["doctor_name"]`, `fields["prescription_url"]`), retry with the SAME idempotency key only if the request body is unchanged, otherwise a new key; `ACCOUNT_NOT_APPROVED` -> "awaiting approval", allow retry later; `ACCOUNT_BLOCKED` -> blocked message + `logout()`; `VALIDATION_ERROR` -> map `fields` to form errors (`email`, `password`, `otp`, `current_password`, ...); `NOT_FOUND` -> go back / refresh list; `INVALID_TRANSITION` -> order state changed, reload the order (also invoice before sold); `CONFLICT` on signup -> `fields["email"]` already registered; `RATE_LIMITED` -> wait / request a new OTP; `UNAUTHORIZED` on login -> bad credentials.
- List state: `items: List<T>`, `pagination: Pagination?`, `isLoadingMore: Boolean`, `endReached: Boolean`, `lastRequest: PageRequest`. First page: `PageRequest()`. More: `loadMore(items, lastRequest, pagination) { req -> ... }` needs an `ApiResult`; with Flows simply call `lastRequest.next(pagination)` (null = `endReached = true`) and append `PagedData.items`. Reset the list when the filter changes.

## 5. Events and flows
- Inject `AppEvents` and collect `events: SharedFlow<AppEvent>` in one place (activity/app ViewModel): `UpgradeRequired(minVersion)` -> blocking update screen; `SessionInvalid` -> clear local state and go to login; `AccountBlocked` -> message + `logout()`; `RateLimited` -> snackbar.
- Role gate after `verifyOtp`: if `TokenData.role != "user"` -> `repository.logout().collect { }` and show "not a customer account"; otherwise `setUserLoggedIn()`.
- Idempotency (createOrder): `IdempotencyKeyProvider.newKey()` ONCE per checkout tap, store in the screen/VM state, reuse on retry after Network/Timeout/5xx, drop (null) on Success or when the user edits the order.
- Signup: account starts UNAPPROVED. After `signup` Success tell the user an admin must approve; `login` returns `ACCOUNT_NOT_APPROVED` until then.
- On app start: `getAppConfig()` (compare `minAppVersion`, cache enums/limits); after login `registerDevice(DeviceRegisterRequest(fcmToken, "android", BuildConfig.VERSION_NAME))`.

## 6. Breakages (`file:line  old call -> new method`)
- viewModel/MyViewModel.kt:128  `repository.createUser(...)` -> `signup(UserCreateRequest, image)`
- viewModel/MyViewModel.kt:154  `repository.loginUser(...)` -> `login(email, password)`
- viewModel/MyViewModel.kt:182  `repository.verifyUserOtp(...)` -> `verifyOtp(accountId, otp)` (also remove VM `tokenManager.saveTokens` at :197)
- viewModel/MyViewModel.kt:214  `repository.requestUserPasswordReset(...)` -> `requestPasswordReset(email)`
- viewModel/MyViewModel.kt:232  `repository.resetUserPasswordWithOtp(...)` -> `confirmPasswordReset(email, otp, newPassword)`
- viewModel/MyViewModel.kt:250  `repository.getSpecificUser(...)` -> `getMe()`
- viewModel/MyViewModel.kt:285  `repository.getAllProducts()` -> `getProducts(filter, page)`
- viewModel/MyViewModel.kt:302  `repository.getSpecificProduct(...)` -> `getProduct(id)`
- viewModel/MyViewModel.kt:323  `repository.createOrder(userId, productId, quantity, message)` -> `createOrder(OrderCreateRequest, prescription, idempotencyKey)`
- viewModel/MyViewModel.kt:343  `repository.getUserOrders(...)` -> `getMyOrders(filter, page)`
- viewModel/MyViewModel.kt:360  `repository.getOrderById(...)` -> `getOrder(id)`
- viewModel/MyViewModel.kt:377  `repository.getSellHistoryByUserId(...)` -> `getMyOrders(OrderFilter(status = SOLD))` (+ `getInvoice`)
- viewModel/MyViewModel.kt:425  `repository.getStoredToken()` -> removed
- viewModel/MyViewModel.kt:429  `repository.getCurrentUserId()` -> `currentAccountId()`
- viewModel/MyViewModel.kt:433  `repository.getCurrentUserRole()` -> `currentRole()`
- viewModel/MyViewModel.kt:437  `repository.logout()` -> compiles but does nothing unless collected: `repository.logout().collect { }`
- All state classes in `utils/screensState/*` (CreateUserState, LoginUserState, VerifyOtpState, PasswordReset*State, UserState, AllProductState, GetSpecificProductState, CreateOrderState, GetUsersOrdersState, GetOrderByIdState, GetUserSellHistoryState) reference old `network/response/*` types and must be re-typed to the DTOs above. UI/screens that read old fields (`user_id`, `Product_id`, `date_of_order_creation`, `sold`, ...) follow in Part 4.

## 7. Hilt
- `Repository @Inject constructor(AuthApi, UsersApi, ProductsApi, OrdersApi, DevicesApi, MetaApi, HealthApi, TokenManager)`, `@Singleton`. All provided by `ApiProvider` / `V1ApiModule`; never construct manually.
- ViewModels inject: `Repository`, `AppEvents`, `IdempotencyKeyProvider` (both `@Singleton @Inject`). `TokenManager` is no longer needed in the ViewModel (the Repository owns session saving).
