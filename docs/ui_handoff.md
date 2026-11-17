# UI handoff (CUSTOMER APP)

## Part 1 — Splash, SignIn, SignUp, Otp | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens restyled: SplashScreen -> ui/screens/SplashScreen.kt, SignIn -> ui/screens/SignIn.kt, SignUp -> ui/screens/SignUp.kt, OtpScreen -> ui/screens/OtpScreen.kt (all ported from MediAdminApp patterns, blue tokens) | MISSING screens: none | Skipped/notes: device register after OTP (`POST /devices`) not wired: project has no push-token source (no Firebase/FCM dependency, gradle edits not allowed in this task).

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/theme/Color.kt, Theme.kt, Shape.kt, Type.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/components/Press.kt, Controls.kt, DetailParts.kt, StateComponents.kt, StatusChip.kt, AppScaffolds.kt, ListParts.kt, FormParts.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/adaptive/Adaptive.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/device/DeviceAccess.kt, UploadGuard.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/AuthCommon.kt, SplashScreen.kt, SignIn.kt, SignUp.kt, OtpScreen.kt
- app/src/main/java/com/saurabh/mediuserapp/MainActivity.kt
- app/src/main/res/values/themes.xml, values/colors.xml (brand_background, splash_background), drawable/ic_splash_logo.xml; AndroidManifest.xml (MainActivity theme `Theme.MediUserApp.Starting`)
- NOT locked (edited, later parts extend): ui/nav/Routes.kt, ui/nav/NavApp.kt

Signatures:
- Theme: `MediUserAppTheme(content)` (light only, no dynamic colour, `LocalRippleConfiguration = null`). Tokens (Color.kt, only place with hex): `Blue50..Blue900`, `Green50/100/200/600/700/800` (success only), `Amber50/100/200/400/600/700/800`, `Red50/100/200/600/700/800`, `Sky50/100/200/600/700/800`, `White, IconFill, FieldFill, Ink, Muted, Outline, Transparent`, logo-only `LogoBlue, LogoCyan, LogoLavender`. `data class Shade(normal, pressed, held)`; `Shades.{BlueSolid, BlueRow, BlueChipSelected, BlueSecondaryButton, IconButton, BlueText, SuccessPill, SuccessCard, GreenText, AmberSolid, AmberPill, AmberCard, AmberRow, AmberText, RedSolid, RedPill, RedCard, RedText, SkyTile, SkyCard, SkyText}`.
- Theme.kt: `StatusColor(container, content)`, `StatusColors(successBg/Text, warningBg/Text(Amber700), dangerBg/Text(Red700), infoBg/Text(Sky700), neutralBg/Text).of(OrderStatus)`, `MaterialTheme.statusColors`, `MaterialTheme.isLight`, `GsUi.{rowShade(), secondaryButtonShade(), iconButtonShade(), familyShade(shade), fieldColors(), topBarColors()}`. Shapes/Typography = M3 defaults.
- Press.kt: `enum PressLevel`, `InteractionSource.pressLevel()`, `Shade.at(level)`, `InteractionSource.shadeColor(shade, label)`, `InteractionSource.pressScale(enabled)`.
- Controls.kt: `GsTextField(...)` (OutlinedTextField params), `GsFilterChip(selected, onClick, label, modifier, enabled, leadingIcon, trailingIcon)`, `LinkButton(text, onClick, modifier, enabled, shade = Shades.BlueText)`, `NeutralTextButton(text, onClick, modifier, enabled)`, `GsFab(text, icon, onClick, modifier, expanded)`, `ChipLabel(text)`.
- DetailParts.kt: `DetailCard(modifier, title?, icon?, content)`, `InfoTile(label, value, modifier, icon?)`, `KeyValueRow(label, value, modifier, emphasize)`, `enum Tone{Success, Warning, Danger, Info}`, `ToneChip(text, tone, modifier)` (Success Green100/600; Warning/Danger/Info 100 + 700 bold), `PersonAvatar(url, name, modifier, size=96dp)`, `MedicineImage(url, modifier, size=64dp)` (1:1 Fit, Sky100 + Medication fallback), `ShadeButton(text, onClick, modifier, shade=Shades.BlueSolid, enabled, isLoading, icon?)`, `ShadeOutlinedButton(text, onClick, modifier, enabled, icon?, contentColor)`, `NavRow(title, onClick, modifier, subtitle?, icon?)`, `Modifier.shadeRow(onClick, onLongClick?, shade?)`, `TileIconButton(icon, contentDescription, onClick, modifier, enabled)`, `Modifier.clearFocusOnTap()`.
- StateComponents.kt: `LoadingState(modifier)`, `EmptyState(title, message, modifier, action?)`, `ErrorState(message, onRetry, modifier)`, `SectionHeader(text, modifier)`, `ConfirmDialog(title, message, confirmText, onConfirm, onDismiss, dismissText="Cancel", destructive=true)`.
- StatusChip.kt: `StatusChip(status: OrderStatus, modifier)`, `OrderStatus.label()`.
- AppScaffolds.kt: `AppScreenScaffold(title, modifier, onBack?, navigationIcon?, actions, scrollBehavior, floatingActionButton, bottomBar, snackbarHostState?, content)`, `ConnectivityBanner(modifier, onRetry?)`, `ScreenStateHost(isLoading, error, isEmpty, onRetry, modifier, emptyTitle, emptyMessage, content)`, `SkeletonList(rows, modifier)`, `PagedLazyColumn(isRefreshing, onRefresh, isLoadingMore, endReached, onLoadMore, modifier, state, contentPadding, verticalArrangement, content)`, `FormScaffold(modifier, content)`, `Map<String,String>?.fieldError(key)`, `StatCard(label, value, icon, onClick?, modifier, highlight, tone?)`, `HeroCard(title, value, modifier, subtitle?, icon?, onClick?)` (only gradient Blue800->600->400), `ImageSourceSheet(onDismiss, onCamera, onGallery, onRemove?, title)`.
- ListParts.kt: `ThumbImage(url, fallback, modifier, size)`, `Instant?.dayLabel()`, `Instant?.dateTimeLabel()`. FormParts.kt: `LocalDate?.showDate()`, `DateField(label, iso, onIsoChange, enabled, error, modifier)`, `SaveBar(text, loading, onClick, enabled)`.
- Adaptive.kt: `WindowSize`, `WindowInfo`, `rememberWindowSize()`, `AdaptiveWidths.{Form 560dp, List 840dp}`, `AdaptiveContent(modifier, maxWidth, content)`, `adaptiveGridCells(minCellWidth)`, `TwoPane(showDetail, list, detail, modifier, emptyDetail)`.
- device/: `rememberPermissionState(permission, onResult): PermissionHandle(status, isGranted, request(), openSettings())`, `PermissionStatus`, `Context.openAppSettings()`, `Context.hasCamera()`, `notificationPermissionOrNull()`, `rememberIsOnline()`, `prepareUpload(context, uri, uploads: UploadConfig?, imageOnly, maxPx, quality): UploadResult(Ready(part: ImagePart) | Rejected(message))`, `AppError?.uploadMessage()`.
- AuthCommon.kt (internal, ui.screens): `AUTH_MESSAGE_KEY`, `AppError?.fieldError(key)`, `AuthFormColumn(modifier, verticalArrangement, content)`, `SupportingText(error, hint?)`, `PasswordField(value, onValueChange, label, error, modifier, hint?, imeAction, onDone?)`, `AuthTopBar(title, onBack)`, `AuthHeader(icon, title, subtitle, modifier)`, `AuthFooterLink(prompt, action, onClick, modifier)`, `AuthFormError(text)`, `AuthPrimaryButton(text, isLoading, onClick, modifier)`.
- Screens: `SplashScreen(navController, viewModel)`, `SignIn(viewModel, navController)`, `SignUp(viewModel, navController)`, `OtpScreen(accountId, viewModel, navController)`.
- Routes (`Routes.*`, type-safe): `SplashRoutes, SignInRoutes, SignUpRoutes, VerifyOtpRoutes(accountId), WaitingRoutes(email?), ForgotPasswordRoutes, ForgotPasswordOtpRoutes(userId), ResetPasswordRoutes(userId), ProductRoutes, OrdersRoutes, HistoryRoutes, ProfileRoutes, SpecificProductRoutes(productId)`.

ViewModel changes: none (uses existing `isUserLoggedIn`, `upgradeRequiredVersion`, `appConfigState`, `getAppConfig()`, `loginUserState/loginUser/resetLoginState`, `createUserState/createUser(..., image)/resetCreateUserState`, `verifyOtpState/verifyUserOtp/resetVerifyOtpState`, `messages`).

Navigation changes:
- Routes.kt rewritten: removed unused `OtpRoutes`, `EachUserOrderRoutes`, string `route` companions; `VerifyOtpRoutes(userId)` -> `VerifyOtpRoutes(accountId)`; `WaitingRoutes(userId)` -> `WaitingRoutes(email: String? = null)` (now wired, old WaitingScreen receives the email).
- NavApp.kt rewritten: start = Splash; NavHost transitions (forward slide R->L 300ms + fade, exit -1/4 + fade 0.85; pop reverse 280ms; tab<->tab crossfade 200ms); bottom bar (Home=Products, Orders, History, Profile; custom nav item, no ripple, Blue100 selected pill, saveState/restoreState/launchSingleTop); global snackbar from `viewModel.messages`; session guard (logged out on a non-auth route -> SignIn, `popUpTo(0)`).
- Edges: Splash -> Products / SignIn (`popUpTo(Splash)`), SignIn -> Otp / SignUp / ForgotPassword / Waiting (ACCOUNT_NOT_APPROVED), SignUp -> SignIn (message), Otp -> Products (`popUpTo(0)`), Otp -> SignIn (5 wrong tries / RATE_LIMITED / role != user / resend), Otp -> Waiting (ACCOUNT_NOT_APPROVED).
- MainActivity: `installSplashScreen()`, edge-to-edge transparent bars.

PENDING_EDGES: Splash->UpgradeRequired (Part 3; now inline "update required" message on Splash), SignIn->AccountBlocked (Part 3; now form message), Otp->AccountBlocked (Part 3; now back to SignIn with message), NavApp global 401/403/426 events -> SignIn/AccountBlocked/UpgradeRequired (Part 3), Waiting "check again" -> Otp (Part 2), Products->ProductDetail (Part 3).

Theme additions: complete theme foundation (project still had the default purple template): Color.kt (44-colour blue guide + `Transparent` + Shade/Shades), Theme.kt (light scheme per ui_rules 6.3 + surfaceContainer roles, StatusColors, GsUi, no ripple), Shape.kt, Type.kt (M3 defaults); res splash theme + colors.

## Part 2 — ForgotPassword, ForgotPasswordOtp, ResetPassword, Waiting | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens restyled: ForgotPasswordScreen -> ui/screens/ForgotPasswordScreen.kt, ForgotPasswordOtpScreen -> ui/screens/ForgotPasswordOtpScreen.kt, ResetPasswordScreen -> ui/screens/ResetPasswordScreen.kt, WaitingScreen -> ui/screens/AccountStatusScreens.kt (old WaitingScreen.kt deleted) | MISSING screens: none | Skipped/notes: Waiting "Check again" returns to SignIn with a hint (SignIn keeps the typed email/password in saved state; credentials are not stored anywhere else), user taps Sign in -> Otp.

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/ForgotPasswordScreen.kt, ForgotPasswordOtpScreen.kt, ResetPasswordScreen.kt, AccountStatusScreens.kt (Part 3 adds UpgradeRequired/AccountBlocked into AccountStatusScreens.kt)
- Deleted (unused after port): ui/screens/WaitingScreen.kt, ui/components/AuthComponents.kt, ui/components/SpecificUser.kt

Signatures:
- `ForgotPasswordScreen(viewModel, navController)` -> request code, success -> `ForgotPasswordOtpRoutes(email)`; reads `AUTH_MESSAGE_KEY` (rate-limit message from Reset).
- `ForgotPasswordOtpScreen(email, viewModel, navController)` -> collects 6-digit code (no API), resend = `requestPasswordReset(email)` with 60 s cooldown, Continue -> `ResetPasswordRoutes(email, otp)`; reads `AUTH_MESSAGE_KEY` (invalid code message from Reset).
- `ResetPasswordScreen(email, otp, viewModel, navController)` -> `resetPasswordOtp(email, otp, newPassword)`; success -> SignIn (message); `fields.otp` -> back to ForgotPasswordOtp (message); RATE_LIMITED -> back to ForgotPassword (message); `fields.new_password` under the field.
- `internal StatusMessageScreen(icon, tone, title, message, actionText, onAction, secondaryText?, onSecondary?)` (AccountStatusScreens.kt; tone tile Red/Amber/Sky 100 + 700, Success Green).
- `WaitingScreen(email, navController)`; back = SignIn.
- `internal OtpBoxes(value, length, isError, enabled, focusRequester, onValueChange, onDone)` in OtpScreen.kt (visibility changed from private to internal for reuse; logged exception to the Part 1 lock, no logic change).

ViewModel changes: none (uses `passwordResetState/requestPasswordReset/resetPasswordResetState`, `passwordResetOtpState/resetPasswordOtp/resetPasswordResetOtpState`).

Navigation changes: `ForgotPasswordOtpRoutes(userId)` -> `ForgotPasswordOtpRoutes(email)`; `ResetPasswordRoutes(userId)` -> `ResetPasswordRoutes(email, otp)`; Waiting now gets `navController`. Edges: SignIn -> Forgot -> ForgotOtp -> Reset -> SignIn; Reset -> ForgotOtp (otp invalid); Reset -> Forgot (rate limited); Waiting -> SignIn (check again / back).

PENDING_EDGES: Splash->UpgradeRequired, SignIn->AccountBlocked, Otp->AccountBlocked, NavApp global 401/403/426 (Part 3); Products->ProductDetail (Part 3).

Theme additions: none.

## Part 3 — UpgradeRequired, AccountBlocked, ProductScreen, ProductDetail (+ ProductFilterSheet) | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens restyled: ProductScreen -> ui/screens/ProductScreen.kt (rewritten, admin ProductScreen pattern); new: UpgradeRequiredScreen + AccountBlockedScreen -> ui/screens/AccountStatusScreens.kt, ProductDetailScreen -> ui/screens/ProductDetailScreen.kt | MISSING screens: none | Skipped/notes: barcode icon on Products and Buy button on ProductDetail not shown yet (targets BarcodeScanner/PlaceOrder are Part 4; no stubs). Admin `FullscreenImagePreviewDialog` not copied (raw colours); replaced by token-based `ImageZoomDialog`.

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/ProductScreen.kt, ProductDetailScreen.kt, AccountStatusScreens.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/components/ProductParts.kt, ProductFilterSheet.kt, ImageZoomDialog.kt
- app/src/main/java/com/saurabh/mediuserapp/utils/StringExtensions.kt

Signatures:
- `ProductScreen(viewModel, navController)`: AppScreenScaffold "Medicines" (filter tile action), search (400 ms debounce -> `setProductFilter(q)`), chips (In stock, No prescription, categories), adaptive grid 320dp, pull-to-refresh, load-more, skeleton/error/empty; row tap -> `SpecificProductRoutes(productId)`.
- `ProductDetailScreen(productId, viewModel, navController)`: 220dp 1:1 Fit image (tap -> zoom), name/generic/strength, pills (stock, Rx Required, Schedule), Price card (price, MRP, GST included), Details tile grid; `purchase_price` never shown; loading/error(Retry), pull-to-refresh.
- `UpgradeRequiredScreen(viewModel)` (back disabled, Play Store intent), `AccountBlockedScreen(viewModel, navController)` (back disabled, Log out -> `acknowledgeAccountBlocked()` -> SignIn `popUpTo(0)`).
- Components: `ProductImage(url, dosageForm, modifier, size=64dp)` (Fit; fallback Medication Sky / WaterDrop Blue / Vaccines Amber / Healing Sky), `ProductStockChip(p)` (Out of stock / Only N left (sellable_stock <= reorder_level) / In stock), `Double.inr()`, `ProductFilterSheet(filter, sort, order, categories, onApply(filter, sort, order), onDismiss)` (sort name/price/mrp/updated_at + asc/desc, in stock, Rx any/required/none, schedule, category; Reset/Apply), `ImageZoomDialog(imageUrl, onDismiss)` (pinch/double-tap zoom, pan, close tile). Utils: `String.capitalizeEachWord()`.

ViewModel changes:
- `productFilter: StateFlow<ProductFilter>`, `productSort: StateFlow<PageRequest>` (sort/order), `setProductFilter(filter, sort, order)` (reload page 1).
- `getAllProduct(filter = current, force = false)` (skips reload when data exists and filter unchanged; uses current sort), `loadMoreProducts()` uses current filter.
- `categoriesState: StateFlow<UiState<List<ProductCategoryDto>>>`, `loadProductCategories(force = false)` (limit 100, sort name).
- `accountBlocked: StateFlow<Boolean>` (set by `AppEvent.AccountBlocked` instead of auto logout + snackbar), `acknowledgeAccountBlocked()` (clears flag, `logout()`).

Navigation changes: + `UpgradeRequiredRoutes`, `AccountBlockedRoutes` (both treated as auth routes: no bottom bar, not hit by the session guard); + `composable<SpecificProductRoutes>` -> ProductDetailScreen. Global events in NavApp: `upgradeRequiredVersion != null` -> UpgradeRequired (`popUpTo(0)`, covers Splash min-version and any 426); `accountBlocked` -> AccountBlocked (`popUpTo(0)`, covers SignIn/Otp/any 403 ACCOUNT_BLOCKED); 401 TOKEN_INVALID / refresh failure -> `isUserLoggedIn=false` -> SignIn (existing guard, now skipped while blocked/upgrade).
Resolved PENDING_EDGES: Splash->UpgradeRequired, SignIn->AccountBlocked, Otp->AccountBlocked, global 401/403/426, Products->ProductDetail.

PENDING_EDGES: Products->BarcodeScanner (Part 4), ProductDetail->PlaceOrder (Part 4).

Theme additions: none.

## Part 4 — BarcodeScanner, PlaceOrder, OrderPlaced, Orders (+ PrescriptionSourceSheet) | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens restyled: OrdersScreen -> ui/screens/OrdersScreen.kt (rewritten, bound to `userOrdersState`); new: BarcodeScannerScreen (ported from admin, "Add product" removed), PlaceOrderScreen, OrderPlacedScreen | MISSING screens: none | Skipped/notes: uCrop not used for prescription images (images are downscaled/re-encoded by `prepareUpload`); Orders row tap and OrderPlaced "View order" wait for OrderDetail (Part 5) — OrderPlaced primary button opens the Orders tab instead; rows render without click (no stub).

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/BarcodeScannerScreen.kt, PlaceOrderScreen.kt, OrderPlacedScreen.kt, OrdersScreen.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/components/OrderParts.kt
- app/src/main/java/com/saurabh/mediuserapp/utils/screensState/BarcodeLookupState.kt
- Edited (pending-edge wiring only): ProductScreen.kt (scan barcode tile action), ProductDetailScreen.kt (bottom `SaveBar` "Buy · price" / disabled "Out of stock" -> PlaceOrder)

Signatures:
- `BarcodeScannerScreen(viewModel, navController)`: CameraX + ML Kit (EAN/UPC/Code128/Code39/QR/DataMatrix), torch toggle, camera permission prompt, manual entry + Look up; Found -> `SpecificProductRoutes(id)` popping the scanner; NotFound -> "Product not found" card + Scan again; Failed -> Retry.
- `PlaceOrderScreen(productId, viewModel, navController)`: product summary, `QuantityStepper` (1..min(sellable_stock, 100)), unit price + total, Rx section when `requires_prescription` (doctor name required, patient optional, prescription file required via `PrescriptionSourceSheet`; `prepareUpload(imageOnly=false)` checks type/size from app-config), note, `SaveBar("Place order · total")`. Errors: OUT_OF_STOCK -> `fields.quantity` under qty; PRESCRIPTION_REQUIRED -> doctor/prescription errors; VALIDATION fields; 413/415 -> prescription error. Idempotency: key from VM kept across retries; any field change after a failed attempt drops it (`resetCreateOrderState()`) before the next submit; state reset once per screen entry.
- `OrderPlacedScreen(orderId, viewModel, navController)`: success status screen (Green tile), order summary, "View my orders" (Orders tab), "Continue shopping" / back -> Products; resets `createOrderState` on dispose.
- `OrdersScreen(viewModel, navController)`: "My orders", chips All/Pending/Approved/Cancelled, `PagedLazyColumn` (pull-to-refresh, load-more), skeleton/error/empty, `OrderCard`.
- Components (OrderParts.kt): `QuantityStepper(value, onValueChange, min, max, enabled, modifier)`, `PrescriptionSourceSheet(onDismiss, onCamera, onGallery, onPdf, onRemove?)`, `OrderCard(order, onClick: (() -> Unit)?, modifier)` (image, product, qty × price, date, "Prescription needed" warning when pending Rx without file, total + StatusChip).

ViewModel changes:
- `barcodeLookup: StateFlow<BarcodeLookupState>` (Idle/Loading/Found/NotFound/Failed), `lookupBarcode(code)`, `clearBarcodeLookup()`.
- `orderFilter: StateFlow<OrderFilter>`; `getUserOrders(filter = current, force = true)` (force=false skips reload when data exists for the same filter).
- `createOrder(...)` ignores calls while loading.

Navigation changes: + `BarcodeScannerRoutes`, `PlaceOrderRoutes(productId)`, `OrderPlacedRoutes(orderId)`; OrdersRoutes now `OrdersScreen(viewModel, navController)`. Edges: Products -> BarcodeScanner -> ProductDetail; ProductDetail -> PlaceOrder -> OrderPlaced (`popUpTo(PlaceOrder){inclusive}`); OrderPlaced -> Orders tab / Products.
Resolved PENDING_EDGES: Products->BarcodeScanner, ProductDetail->PlaceOrder.

PENDING_EDGES: Orders->OrderDetail, OrderPlaced->OrderDetail ("View order"), History->OrderDetail (Part 5/6).

Theme additions: none.

## Part 5 — OrderDetail (+ EditOrderInfoSheet, CancelOrderDialog), UploadPrescription, PrescriptionViewer, Invoice | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens: all new (ported from admin SpecificOrderScreen / PrescriptionViewerScreen / InvoiceScreen, admin actions removed) | MISSING screens: none | Skipped/notes: none.

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/OrderDetailScreen.kt, UploadPrescriptionScreen.kt, PrescriptionViewerScreen.kt, InvoiceScreen.kt
- app/src/main/java/com/saurabh/mediuserapp/ui/components/OrderDialogs.kt
- Edited: components/OrderParts.kt (+ public `SelectedFileRow`, moved from PlaceOrderScreen.kt which now imports it), OrdersScreen.kt (row tap -> OrderDetail), OrderPlacedScreen.kt (primary "View order" -> OrderDetail, pops OrderPlaced), repo/Repository.kt, viewModel/MyViewModel.kt, utils/screensState/ActionStates.kt

Signatures:
- `OrderDetailScreen(orderId, viewModel, navController)`: header card (order id, StatusChip, Placed->Approved->Sold track, "Being prepared" for approved, cancel reason for cancelled), Product card (image, name -> ProductDetail, unit price, qty, total), Prescription card (Rx/verified chips, "View prescription" -> viewer or "not uploaded"), Order info card. Top-bar Edit tile (pending). Bottom bar by status: pending = Cancel order (red outlined, `CancelOrderDialog`) + Upload/Replace Rx (Rx products); sold/returned = View invoice; cancelled = Order again (-> PlaceOrder); approved = none. INVALID_TRANSITION on cancel/edit -> reload + snackbar. Pull-to-refresh, loading/error.
- `UploadPrescriptionScreen(orderId, viewModel, navController)`: order summary, file via `PrescriptionSourceSheet` + `SelectedFileRow`, doctor/patient prefilled, `SaveBar("Upload")`; `prepareUpload(imageOnly=false)`; 413/415/`fields.prescription` under the file; INVALID_TRANSITION -> message + reload; success -> back to OrderDetail (detail state already updated). Pending orders only.
- `PrescriptionViewerScreen(orderId, viewModel, navController)`: verified/awaiting chip + doctor, zoomable Fit image or PDF card (open externally), empty state when none; read-only.
- `InvoiceScreen(orderId, viewModel, navController)`: invoice card, Bill to, items (batch chip, expiry, qty x unit, GST, line total), Totals; Share (top tile + SaveBar); INVALID_TRANSITION -> "available once sold".
- Components (OrderDialogs.kt): `CancelOrderDialog(busy, onConfirm(reason?), onDismiss)` (red confirm, "Keep order"), `EditOrderInfoSheet(patient0, doctor0, busy, error, patientError, doctorError, onSave(patient?, doctor?), onDismiss)`. OrderParts.kt: `SelectedFileRow(uri, enabled, onChange)`.

ViewModel changes:
- Repository: `updateOrder(orderId, patientName?, doctorName?)` (PATCH orders/{id}).
- VM: `editOrderState: StateFlow<EditOrderState>`, `updateOrderInfo(orderId, patientName?, doctorName?)` (updates detail + refreshes list), `resetEditOrderState()`, `resetCancelOrderState()`, `resetUploadPrescriptionState()`; `getOrderById` keeps the loaded order during reload/error (no flicker); `uploadPrescription` success refreshes the orders list.
- `EditOrderState(isLoading, success: OrderDto?, error, appError)` in ActionStates.kt.

Navigation changes: + `OrderDetailRoutes(orderId)` (replaces old EachUserOrderRoutes), `UploadPrescriptionRoutes(orderId)`, `PrescriptionViewerRoutes(orderId)` (order id instead of URL; viewer reads the loaded order), `InvoiceRoutes(orderId)`. Edges: Orders -> OrderDetail; OrderPlaced -> OrderDetail; OrderDetail -> ProductDetail / UploadPrescription / PrescriptionViewer / Invoice / PlaceOrder (order again).
Resolved PENDING_EDGES: Orders->OrderDetail, OrderPlaced->OrderDetail.

PENDING_EDGES: History->OrderDetail (Part 6).

Theme additions: none.

## Part 6 — History, Profile, EditProfile, ChangeCredentials | Status: DONE (build OK)

Part status: DONE | Files changed (LOCKED): see list | Screens restyled: HistoryScreen -> ui/screens/HistoryScreen.kt (rewritten, orders API instead of sell history), ProfileScreen -> ui/screens/ProfileScreen.kt (rewritten, admin ProfileScreen pattern); new: EditProfileScreen, ChangeCredentialsScreen | MISSING screens: none | Skipped/notes: Settings button on Profile not shown yet (SettingsAbout is Part 7; no stub).

LOCKED:
- app/src/main/java/com/saurabh/mediuserapp/ui/screens/HistoryScreen.kt, ProfileScreen.kt, EditProfileScreen.kt, ChangeCredentialsScreen.kt
- Deleted: utils/screensState/GetUserSellHistoryState.kt (replaced by `historyState`)

Signatures:
- `HistoryScreen(viewModel, navController)`: chips Purchased (sold, default) / Returned, From/To `DateField`s (+ Clear dates, from<=to check), `PagedLazyColumn` (refresh, load-more), `OrderCard` tap -> `OrderDetailRoutes(orderId)` (invoice from there).
- `ProfileScreen(viewModel, navController)`: avatar, name, Approved/Awaiting/Blocked chips, Account details (email, phone, address, pin, customer id, member since), Edit profile (button + top tile), "Change email or password", Log out (`ConfirmDialog`, red) -> `logout()` + SignIn `popUpTo(0)`; snackbar for messages handed back via `AUTH_MESSAGE_KEY`; pull-to-refresh, loading/error.
- `EditProfileScreen(viewModel, navController)`: photo (`ImageSourceSheet`, camera/gallery, `prepareUpload(imageOnly=true)`), name, phone (`^\+?[0-9]{7,15}$`), address, pin (`^[0-9]{4,10}$`); sends only changed fields (`ProfileUpdateRequest`) + optional `image`; backend `fields` under inputs; success -> back to Profile ("Profile updated").
- `ChangeCredentialsScreen(viewModel, navController)`: new email (optional, must differ), new password + confirm (8-128), current password (required); `fields.email` (CONFLICT), `fields.password`, `fields.current_password` / UNAUTHORIZED under inputs; success -> back to Profile with message (email change: "use the new email next time").

ViewModel changes:
- Removed `sellHistoryState` / `getSellHistoryByUserId()`; added `historyState: StateFlow<GetUsersOrdersState>`, `historyFilter: StateFlow<OrderFilter>` (default status SOLD), `getHistoryOrders(filter = current, force = true)`, `loadMoreHistory()`.
- `resetUpdateProfileState()`; `updateMe()` ignores calls while loading.
- `logout()` now also `resetAllState()` (no previous user's data after sign-out).

Navigation changes: + `EditProfileRoutes`, `ChangeCredentialsRoutes`; History/Profile composables now `(viewModel, navController)`. Edges: History -> OrderDetail; Profile -> EditProfile / ChangeCredentials (back with message) / SignIn (logout, `popUpTo(0)`).
Resolved PENDING_EDGES: History->OrderDetail.

PENDING_EDGES: Profile->Settings (Part 7).

Theme additions: none.

## Part 7 — SettingsAbout + final navigation audit | Status: DONE (build OK, 37 JVM tests pass)

Part status: DONE | Files changed (LOCKED): ui/screens/SettingsAboutScreen.kt | Screens: new SettingsAboutScreen | MISSING screens: none | Skipped/notes: dark theme not offered (colour guide is light-only; Appearance shows "Light theme").

Signatures:
- `SettingsAboutScreen(viewModel, navController)`: Account card (Edit profile, Email & password `NavRow`s), App card (Appearance, app version + API version from app-config), About card (logo, app name, version chip, description).

ViewModel changes: none.

Navigation changes: + `SettingsRoutes` -> SettingsAboutScreen; Profile gets "Settings & about" outlined button.
Resolved PENDING_EDGES: Profile->Settings.

Navigation audit (flow doc section 2 + back-stack rules):
- 25 routes declared = 25 `composable<>` destinations; every route is navigated to from at least one screen; no orphan screens (WaitingScreen / UpgradeRequiredScreen / AccountBlockedScreen live in AccountStatusScreens.kt).
- Auth graph: Splash -> SignIn / Products / UpgradeRequired; SignIn -> Otp / SignUp / ForgotPassword / Waiting / AccountBlocked (global event); SignUp -> SignIn; Forgot -> ForgotOtp -> Reset -> SignIn (or back to ForgotOtp / Forgot on invalid / rate-limited code); Waiting -> SignIn; Otp -> Products (`popUpTo(0)`) / SignIn / Waiting.
- Main graph: tabs Home(Products) / Orders / History / Profile with crossfade + saveState/restoreState; Products -> Scanner / ProductDetail (+ filter sheet); Scanner -> ProductDetail (scanner popped); ProductDetail -> PlaceOrder -> OrderPlaced (PlaceOrder popped) -> OrderDetail / Products; Orders & History -> OrderDetail -> ProductDetail / UploadPrescription / PrescriptionViewer / Invoice / PlaceOrder; Profile -> EditProfile / ChangeCredentials / Settings / SignIn (logout `popUpTo(0)`).
- Global: 401 TOKEN_INVALID or refresh failure -> SignIn (`popUpTo(0)`); 403 ACCOUNT_BLOCKED -> AccountBlocked; 426 / min_app_version -> UpgradeRequired; Upgrade and Blocked have back disabled; Splash is popped; bottom bar only on the 4 tab routes.
- Colour audit: no `Color(0x..)` / `Color.White/Black/Gray` outside ui/theme; no old green/purple hex anywhere.

PENDING_EDGES: none.

Theme additions: none.
