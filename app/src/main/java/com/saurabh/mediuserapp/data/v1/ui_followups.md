CUSTOMER APP ONLY — UI follow-ups (no UI built in Part 4)

- Awaiting-approval message: login/verify return `ACCOUNT_NOT_APPROVED`; show `loginUserState.error` / `appError.code == ACCOUNT_NOT_APPROVED`. WaitingScreen can no longer poll (no token before approval) — show it after signup success (`createUserState.success`) instead.
- Account blocked: collect `messages` (SharedFlow) in a snackbar host; VM already logs out; `appError.code == ACCOUNT_BLOCKED` on login forms.
- Upgrade required: gate NavApp on `upgradeRequiredVersion != null` with a blocking screen + store link.
- Rate limited: show `messages` as snackbar; on OTP screens `appError.code == RATE_LIMITED` means request a new code.
- Form field errors: render `appError.fields[...]` next to inputs (signup: email/password/phone_number/pin_code; reset: otp/new_password; profile: current_password) from `createUserState`, `loginUserState`, `verifyOtpState`, `passwordResetState`, `passwordResetOtpState`, `updateProfileState`.
- Password reset flow: ForgotPassword -> ForgotPasswordOtp -> ResetPassword still pass the EMAIL in the `userId` route arg; rename the route arg when the nav graph is next touched.
- Prescription upload: file picker -> `Uri.toFilePart(context)` -> `createOrder(..., prescription = file)` or `uploadPrescription(orderId, file, doctorName, patientName)`; observe `uploadPrescriptionState`. On `PRESCRIPTION_REQUIRED` ask for file + doctor name (`appError.fields`).
- Order creation UI: no order button exists on ProductScreen yet; wire `createOrder(...)` + `createOrderState` (OUT_OF_STOCK -> `appError.fields["quantity"]`); call `resetCreateOrderState()` when the user edits the order or leaves the screen.
- Order status chips: use `OrderDto.status` (`OrderStatus` PENDING/APPROVED/SOLD/CANCELLED/RETURNED); old `sold`/`isApproved` booleans are gone.
- Cancel button (pending orders only): `cancelOrder(orderId, reason)` + `cancelOrderState`; `INVALID_TRANSITION` -> reload `getOrderById`.
- Invoice screen: `getInvoice(orderId)` + `invoiceState` (only sold/returned orders).
- Load-more triggers: call `loadMoreProducts()` / `loadMoreOrders()` near list end; read `isLoadingMore`, `endReached`.
- Orders/History screens currently use no VM data: bind them to `userOrdersState` (`success.items`) and `sellHistoryState`; show `createdAt` (Instant) via a date formatter.
- Profile screen: bind to `specificUserState` / `getSpecificUser()` and `updateMe(...)` + `updateProfileState`; image via `Uri.toFilePart`.
- SpecificUser component: ID and Password rows now show "-" (not available in v1); remove them when the layout is next touched.
