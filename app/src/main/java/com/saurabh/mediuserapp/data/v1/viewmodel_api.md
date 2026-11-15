CUSTOMER APP ONLY

# MyViewModel API (v1) — `viewModel/MyViewModel.kt`

## Hilt ctor
`@HiltViewModel MyViewModel @Inject constructor(repository: Repository, appEvents: AppEvents, idempotencyKeyProvider: IdempotencyKeyProvider)` (TokenManager removed).

## State flows
| name | type | success type / extra fields |
|---|---|---|
| isUserLoggedIn | StateFlow<Boolean> | from `repository.isLoggedIn()` |
| upgradeRequiredVersion | StateFlow<String?> | non-null => blocking update screen (min version or "latest") |
| messages | SharedFlow<String> | one-shot snackbar text (rate limited, blocked) |
| appConfigState | StateFlow<UiState<AppConfig>> | |
| createUserState | CreateUserState | success `UserDto`, error, appError |
| loginUserState | LoginUserState | success `LoginData` (use `accountId`), error, appError |
| verifyOtpState | VerifyOtpState | success `TokenData`, error, appError |
| passwordResetState | PasswordResetState | success `PasswordResetRequestData`, error, appError |
| passwordResetOtpState | PasswordResetOtpState | success `Unit?`, error, appError |
| specificUserState | UserState | success `UserDto`, error |
| updateProfileState | UpdateProfileState | success `UserDto`, error, appError |
| getAllProduct | AllProductState | success `PagedData<ProductDto>` (`.items`), pagination, isLoadingMore, endReached, error, appError |
| specificProductState | GetSpecificProductState | success `ProductDto`, error |
| createOrderState | CreateOrderState | success `OrderDto`, error, appError, idempotencyKey |
| userOrdersState | GetUsersOrdersState | success `PagedData<OrderDto>`, pagination, isLoadingMore, endReached, error, appError |
| orderDetailState | GetOrderByIdState | success `OrderDto`, error |
| sellHistoryState | GetUserSellHistoryState | success `PagedData<OrderDto>` (sold orders), error |
| cancelOrderState | CancelOrderState | success `OrderDto`, error, appError |
| uploadPrescriptionState | UploadPrescriptionState | success `OrderDto`, error, appError |
| invoiceState | StateFlow<UiState<InvoiceDto>> | |
All form states keep `isLoading`; `error` is `AppError.userMessage()`; `appError.fields` carries per-input messages.

## Functions
| fn(params) | status | state written | side effects / guards |
|---|---|---|---|
| checkLoginStatus() | UPDATED | isUserLoggedIn | uses repository.isLoggedIn() |
| setUserLoggedIn() / setUserLoggedOut() | UPDATED (out) | isUserLoggedIn | out: repository.clearSession() (local only) |
| createUser(name, password, phoneNumber, email, pinCode, address, image: ImagePart? = null) | UPDATED | createUserState | blank optional fields sent as null; `role` param removed |
| loginUser(email, password) | UPDATED | loginUserState | |
| verifyUserOtp(userId, otp) | UPDATED | verifyOtpState | `userId` = `LoginData.accountId`; repo saves session; role != "user" -> repository.logout() + error "Customer account required"; else setUserLoggedIn() |
| requestPasswordReset(email) | UPDATED | passwordResetState | |
| resetPasswordOtp(email, otp, newPassword) | UPDATED | passwordResetOtpState | first arg now EMAIL (screens still carry it in the `userId` route arg) |
| getSpecificUser() | UPDATED | specificUserState | no params (GET users/me) |
| updateMe(request: ProfileUpdateRequest, image: ImagePart? = null) | NEW | updateProfileState, specificUserState | |
| getAllProduct(filter: ProductFilter = ProductFilter()) | UPDATED | getAllProduct | resets paging to `PageRequest()` |
| loadMoreProducts() | NEW | getAllProduct | guard: isLoading/isLoadingMore/endReached; appends `items` |
| getSpecificProduct(productId) | UPDATED | specificProductState | |
| createOrder(productId, quantity: String, message, doctorName? = null, patientName? = null, prescription: FilePart? = null) | UPDATED | createOrderState | key = state key ?: newKey(); kept on error, cleared on success; success -> getUserOrders() |
| getUserOrders(filter: OrderFilter = OrderFilter()) | UPDATED | userOrdersState | `userId` param removed |
| loadMoreOrders() | NEW | userOrdersState | same guards as products |
| getOrderById(orderId) | UPDATED | orderDetailState | |
| getSellHistoryByUserId() | UPDATED | sellHistoryState | `userId` param removed; status=SOLD |
| cancelOrder(orderId, reason? = null) | NEW | cancelOrderState, orderDetailState | success -> refresh orders list |
| uploadPrescription(orderId, file: FilePart, doctorName? = null, patientName? = null) | NEW | uploadPrescriptionState, orderDetailState | |
| getInvoice(orderId) | NEW | invoiceState | 409 INVALID_TRANSITION until sold |
| getAppConfig() | NEW | appConfigState | runs in init; minAppVersion > BuildConfig.VERSION_NAME => upgradeRequiredVersion |
| resetCreateOrderState() | UPDATED | createOrderState | also drops idempotency key |
| getCurrentUserId() / getCurrentUserRole() | UPDATED | - | repository.currentAccountId() / currentRole() |
| logout() | UPDATED | isUserLoggedIn | repository.logout().collect, then setUserLoggedOut() |
Removed: `getStoredToken()`.

## Removed / changed params
`createUser.role` removed; `getUserOrders.userId`, `getSellHistoryByUserId.userId`, `getSpecificUser.userId` removed; `resetPasswordOtp.userId` -> `email`; `createOrder.userId` removed, `quantity` still String (parsed to Int).

## UI field map old -> new
`LoginUserResponse.user_id` -> `LoginData.accountId`; `VerifyOtpResponse.access_token/refresh_token/message` -> not exposed (`TokenData.role/accountId`); `PasswordResetResponse.user_id/message` -> `PasswordResetRequestData.otpExpiresIn` (no user id); `GetSpecificUserResponse.user` -> `UserDto` directly; `GetAllProductResponse.products` -> `PagedData.items`; `ProductItem` -> `ProductDto` (`Product_id` -> `productId`); `User.user_id/phone_number/pin_code/block/date_of_account_creation` -> `userId/phoneNumber/pinCode/isBlocked/createdAt`; `User.id/password` removed.

## UI call sites changed
- ui/screens/ForgotPasswordScreen.kt: log uses `otpExpiresIn`; navigates with the typed email (no `user_id`)
- ui/screens/OtpScreen.kt: logs `role/accountId` instead of tokens
- ui/screens/ResetPasswordScreen.kt: `resetPasswordOtp(email = userId, ...)`; log
- ui/screens/SignIn.kt: `response.accountId`
- ui/screens/SignUp.kt: removed `role = "user"` arg
- ui/screens/ProductScreen.kt: `ProductDto` types, `.success.items`, preview data
- ui/screens/WaitingScreen.kt: `getSpecificUser()`, `success` is `UserDto`
- ui/screens/ProfileScreen.kt: `viewModel.logout()` (server logout + device unregister)
- ui/components/SpecificUser.kt: `UserDto` fields; ID/Password rows show "-"

## Events
`AppEvents` collected in VM init: SessionInvalid -> reset all states, clearSession, isUserLoggedIn=false; AccountBlocked -> `messages` + logout(); RateLimited -> `messages`; UpgradeRequired -> `upgradeRequiredVersion`.

## Helpers
`private collectInto(onLoading, onError(String?, AppError?), onSuccess)`; `utils/UriExtensions.kt`: `Uri.toFilePart(context): FilePart?` (also an `ImagePart`).

## Remaining errors
None (compile OK, 37 JVM tests pass). Behavior gaps: see ui_followups.md.
