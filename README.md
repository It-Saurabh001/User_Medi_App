# MediUserApp

Android customer app for a pharmacy / medical shop. Customers browse medicines, place orders, upload prescriptions, track order status and view invoices.

It works with a Flask REST API (`/api/v1`) and a separate admin app (MediAdminApp) where the shop owner approves orders and records sales.

## Features

- **Sign up and sign in:** email and password, then a 6-digit OTP (valid 5 minutes). Optional profile photo on sign up. Sessions stay signed in and tokens refresh automatically.
- **Password reset:** email, OTP, new password.
- **Account status:** waiting-for-approval, blocked and "update the app" screens, driven by backend error codes.
- **Products:** search, category chips, filters, pagination and pull-to-refresh. Product detail with stock status and prescription flag. Barcode scanner for quick lookup.
- **Cart and checkout:** add several products and place them together. One checkout gives one invoice.
- **Orders:** place an order, upload a prescription for Rx medicines (camera, gallery or file), edit order info, cancel pending orders. Each order is retry-safe with an idempotency key.
- **Order tracking:** filter by status (Pending, Approved, Cancelled, Sold, Returned) with load more. Order detail shows the actions allowed for the current status.
- **Prescriptions and invoices:** view the uploaded prescription and the invoice of sold orders.
- **History:** sold and returned orders with a date filter.
- **Profile:** view and edit details and photo, change email or password, logout (also unregisters the device), settings and about.

## How it works

```
Customer app (this project)      Backend (Flask)              Admin app
───────────────────────────      ───────────────             ─────────
Place order             ─►       Order = PENDING      ─►     See it in Orders
Upload Rx               ─►       Prescription saved   ─►     Verify prescription
                                 Order = APPROVED     ◄─     Approve
Track status            ◄─       Order = SOLD         ◄─     Mark sold
View invoice            ◄─       Sale + invoice
```

Order status: `PENDING` → `APPROVED` → `SOLD`. An order can also be `CANCELLED` or `RETURNED` (after sold).

**App layers**

```
Compose screens  →  MyViewModel (StateFlow per feature)  →  Repository  →  Retrofit / OkHttp  →  Backend
```

- `Repository` is the single data entry point. Every call returns `Flow<ResultState<T>>`, and errors carry an `AppException(AppError)` built from the backend error code.
- One OkHttp stack: `AuthInterceptor` adds the Bearer token, `TokenAuthenticator` refreshes it on 401 and retries, `VersionInterceptor` sends the app version, `ErrorEventInterceptor` raises global events (session invalid, account blocked, rate limited, upgrade required).
- Tokens are stored with `EncryptedSharedPreferences` in `TokenManager`.
- Every list and detail screen has loading, empty and error states with a Retry button.
- Only users with role `user` can sign in. Admin accounts are rejected.

## Tech stack

| Area | Library |
|---|---|
| Language / UI | Kotlin 2.4.0, Jetpack Compose (BOM 2026.09.00), Material 3 |
| Navigation | Navigation Compose 2.9.8 (type-safe routes, kotlinx serialization) |
| DI | Hilt 2.60.1 |
| Network | Retrofit 3.0.0, OkHttp 5.5.0, Gson |
| Images | Coil 2.7.0, uCrop |
| Scanner | CameraX, ML Kit barcode scanning |
| Storage | EncryptedSharedPreferences |
| Min / target SDK | 31 / 35 (compile 37) |

UI follows a blue design system (Material 3, no ripple, consistent press and hold colours).

## Setup

**Requirements:** Android Studio, JDK 17, and the backend running.

1. Clone the project:
   ```bash
   git clone https://github.com/It-Saurabh001/User_Medi_App.git
   ```
2. Create `secrets.properties` in the project root with your backend URL (this file is not committed):
   ```properties
   # Android emulator
   WirelessPhysicalDevice=http://10.0.2.2:5000/
   # Physical device on the same Wi-Fi (use your computer's IP)
   # WirelessPhysicalDevice=http://192.168.1.100:5000/
   ```
   The app adds `api/v1/` to this URL.
3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

**Note:** a new customer account must be approved by an admin before the first sign in. Until then the app shows the Waiting screen.

## Project structure

```
app/src/main/java/com/saurabh/mediuserapp/
├── data/v1/         API interfaces, DTOs, errors, core types
├── network/         OkHttp, interceptors, token handling
├── repo/            Repository (single data entry point)
├── ui/
│   ├── components/  shared UI parts
│   ├── screens/     all screens
│   ├── nav/         routes and NavHost
│   └── theme/       colours, shapes, typography
└── viewModel/       MyViewModel, state and actions for all screens
```

## Roadmap

**1. Google sign-in**
- "Continue with Google". The backend checks the Google ID token and links it to the account with the same email.

**2. Online payments**
- UPI and card payment for orders through a payment gateway, confirmed by a server webhook.
- Payment status on the order and invoice. Automatic refund when an order is returned.

**3. AI health assistant**
- Ask medicine and health questions in chat. The assistant suggests medicines that are in stock and shows price and prescription requirement.
- Prescription-only medicines are never suggested without a prescription. Emergency symptoms show an emergency message.
- The AI key stays on the backend only.

## License

MIT. See `LICENSE`.

## Author

**Saurabh** - [@It-Saurabh001](https://github.com/It-Saurabh001)
