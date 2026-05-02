# 🔄 Project Update Summary - Authentication & Splash Screen Flow

## ✅ What Was Updated

### 1. **SplashScreen.kt** - Complete Authentication Check
**Function:** Added proper token verification and navigation logic

**Key Changes:**
- ✅ Added `NavController` and `ViewModel` parameters
- ✅ Added delay(2000) followed by **token check logic**
- ✅ If token exists → Navigate to **ProductRoutes** (main app)
- ✅ If no token → Navigate to **SignInRoutes** (login screen)
- ✅ Used `viewModel.isUserLoggedIn.value` to check login status
- ✅ Fixed navigation with `popUpTo` to clear splash from backstack

```kotlin
@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    LaunchedEffect(Unit) {
        delay(2000)
        val isLoggedIn = viewModel.isUserLoggedIn.value
        
        if (isLoggedIn) {
            navController.navigate(Routes.ProductRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
            }
        }
    }
    // UI Code...
}
```

---

### 2. **NavApp.kt** - Updated Splash Navigation
**Function:** Pass required parameters to SplashScreen

**Change Made:**
```kotlin
composable<Routes.SplashRoutes> {
    Log.d(NAV_TAG, "Destination: SplashRoutes")
    SplashScreen(navController, viewModel)  // ✅ Added params
}
```

---

### 3. **Repository.kt** - Token Helper Methods
**Function:** Add token retrieval helper

**New Method Added:**
```kotlin
fun getStoredToken(): String {
    return tokenManager.getAccessToken() ?: ""
}
```

---

### 4. **MyViewModel.kt** - Token Management Methods
**Function:** Expose token-related helpers to UI

**New Methods Added:**
```kotlin
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
```

---

## 🔐 JWT Token Flow Architecture

### **Login/Signup Flow:**
```
1. User Signup/Login
   ↓
2. OTP Verification (after login email+pass)
   ↓
3. Repository.verifyUserOtp() 
   → Saves ACCESS_TOKEN + REFRESH_TOKEN to SharedPreferences
   ↓
4. MyViewModel.setUserLoggedIn() 
   → Sets _isUserLoggedIn = true
   ↓
5. Next App Launch: SplashScreen checks token
   → Token exists? → ProductRoutes (main app)
   → No token? → SignInRoutes (login screen)
```

### **Token Storage (SharedPreferences):**
- **KEY_ACCESS_TOKEN** → Used for API requests (Authorization header)
- **KEY_REFRESH_TOKEN** → Used to refresh expired token
- **KEY_USER_ID** → Current logged-in user ID
- **KEY_USER_ROLE** → User role (admin/user)

### **API Request Handling (AuthInterceptor):**
```
Every API Request:
  ├─ Check if path is PUBLIC (login, signup, OTP, password-reset)
  ├─ If PUBLIC → Send request as-is
  └─ If PRIVATE → Add "Authorization: Bearer {access_token}"
```

---

## 📝 How Token is Used

### **During Authentication:**
1. **Signup** → Create user (no token needed)
2. **Login** → Email + Password sent (no token needed)
3. **Verify OTP** → User ID + OTP sent → **Tokens received & saved**
4. **Future Requests** → Authorization header auto-added

### **Token Expiry Handling:**
- If 401 received → **TokenAuthenticator** attempts refresh
- Uses `refreshToken` to get new `accessToken`
- If refresh fails → Logout user

---

## 🚀 Complete Login/Signup/Reset Flow

### **Sign Up:**
```
SignUp Screen → Name, Email, Password, Phone, Address, PinCode
    ↓
repository.createUser() → API Call
    ↓
Success → Navigate to SignIn
```

### **Sign In:**
```
SignIn Screen → Email, Password
    ↓
repository.loginUser() → API Call
    → Returns: user_id, role (NO tokens yet)
    → Save user_id to SharedPreferences
    ↓
Success → Navigate to OTP Screen (pass user_id)
```

### **OTP Verification:**
```
OtpScreen → Email code from backend
    ↓
repository.verifyUserOtp(userId, otp) → API Call
    → Returns: access_token, refresh_token, role
    → Save ALL tokens to SharedPreferences
    ↓
Success → setUserLoggedIn() = true
    ↓
Navigate to ProductRoutes (main app)
```

### **Forgot Password - Step 1:**
```
ForgotPasswordScreen → Email
    ↓
repository.requestUserPasswordReset(email) → API Call
    → Returns: user_id, message
    ↓
Success → Navigate to ForgotPasswordOtpScreen (pass user_id)
```

### **Forgot Password - Step 2 (OTP):**
```
ForgotPasswordOtpScreen → OTP code
    → (No new API call, just validate OTP format)
    ↓
User enters new password → Navigate to ResetPasswordScreen
```

### **Reset Password - Step 3:**
```
ResetPasswordScreen → New Password
    ↓
repository.resetUserPasswordWithOtp(userId, otp, newPassword)
    ↓
Success → Navigate back to SignIn
    ↓
User can now login with new password
```

---

## 📁 File Access Structure

### **Network Layer (Handles JWT):**
```
network/
├── ApiServices.kt            → API endpoints definition
├── ApiProvider.kt            → Dagger Hilt DI setup
├── TokenManager.kt           → Token storage (SharedPreferences)
├── AuthInterceptor.kt        → Attach token to requests
├── TokenAuthenticator.kt     → Handle token refresh on 401
└── response/                 → API response models
```

### **Repository Layer:**
```
repo/Repository.kt
  ├─ loginUser() → No tokens saved
  ├─ verifyUserOtp() → Tokens SAVED here
  ├─ requestUserPasswordReset()
  ├─ resetUserPasswordWithOtp()
  ├─ isUserLoggedIn() → Check TokenManager
  └─ getStoredToken() → Retrieve token
```

### **ViewModel Layer:**
```
viewModel/MyViewModel.kt
  ├─ loginUser() → Calls Repository
  ├─ verifyUserOtp() → Calls Repository + setUserLoggedIn()
  ├─ resetPasswordOtp()
  ├─ requestPasswordReset()
  ├─ getStoredToken() → Pass-through to Repository
  └─ logout() → Clear tokens + setUserLoggedOut()
```

### **UI Layer:**
```
ui/screens/
├── SplashScreen.kt           → ✅ NOW CHECKS TOKEN & NAVIGATES
├── SignIn.kt                 → Calls viewModel.loginUser()
├── SignUp.kt                 → Calls viewModel.createUser()
├── OtpScreen.kt              → Calls viewModel.verifyUserOtp()
├── ForgotPasswordScreen.kt
├── ForgotPasswordOtpScreen.kt
└── ResetPasswordScreen.kt
```

---

## 🔍 How ApiProvider Connects to Project

### **Dagger Hilt Dependency Injection:**

1. **@Module @InstallIn(SingletonComponent::class)**
   - Makes ApiProvider available app-wide
   - Creates singleton instances (created once, reused forever)

2. **Provides TokenManager:**
   ```kotlin
   @Provides @Singleton
   fun provideTokenManager(@ApplicationContext context: Context): TokenManager
   ```

3. **Provides AuthInterceptor:**
   ```kotlin
   @Provides @Singleton
   fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor
   ```
   - Gets token from TokenManager
   - Adds "Authorization: Bearer {token}" to each request

4. **Provides OkHttpClient:**
   ```kotlin
   @Provides @Singleton
   fun provideHttpClient(
       authInterceptor: AuthInterceptor,
       tokenAuthenticator: TokenAuthenticator
   ): OkHttpClient
   ```
   - Chains interceptors
   - Handles token refresh on 401

5. **Provides Retrofit:**
   ```kotlin
   @Provides @Singleton
   fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit
   ```
   - Uses configured OkHttpClient
   - Sets BASE_URL

6. **Provides ApiServices:**
   ```kotlin
   @Provides @Singleton
   fun provideApiServices(@MainRetrofit retrofit: Retrofit): ApiServices
   ```

### **Injection Chain:**
```
ApiProvider provides all dependencies
    ↓
Repository @Inject constructor(ApiServices, TokenManager)
    ↓
MyViewModel @Inject constructor(Repository, TokenManager)
    ↓
UI Screens access via viewModel
```

---

## ✨ Key Improvements Made

| Issue | Before | After |
|-------|--------|-------|
| SplashScreen stuck | No navigation logic | ✅ Checks token & navigates |
| Token not checked | No auth status check | ✅ Uses viewModel.isUserLoggedIn |
| Navigation params missing | SplashScreen() | ✅ SplashScreen(navController, viewModel) |
| Token management unclear | Mixed concerns | ✅ Centralized in TokenManager |
| Logout flow | Incomplete | ✅ Complete logout() method in ViewModel |

---

## 🧪 Testing the Fix

### **Scenario 1: First App Launch (No Token)**
1. App starts → SplashScreen shows
2. Wait 2 seconds
3. Token check: false
4. Auto-navigate to **SignInRoutes** ✅

### **Scenario 2: After Login (Token Saved)**
1. User completes OTP verification
2. Token saved to SharedPreferences
3. Close and restart app
4. SplashScreen shows
5. Wait 2 seconds
6. Token check: true
7. Auto-navigate to **ProductRoutes** ✅

### **Scenario 3: After Logout**
1. User clicks logout
2. `tokenManager.clearTokens()` called
3. `_isUserLoggedIn.value = false`
4. Navigate to SignInRoutes
5. Restart app
6. Token check: false → SignInRoutes ✅

---

## 📌 Important Notes

1. **Token is AUTOMATICALLY attached** to all authenticated requests via AuthInterceptor
2. **404 users see LoginScreen** automatically on app start
3. **Logged-in users bypass splash** and go directly to main app
4. **Tokens persist** across app restarts (stored in SharedPreferences)
5. **Token refresh is automatic** (handled by TokenAuthenticator on 401)
6. **Password reset doesn't require** being logged in (public endpoint)

---

## 🎯 API Provider Connection Summary

**ApiProvider is the DI backbone that:**
- ✅ Creates TokenManager for token storage
- ✅ Creates AuthInterceptor that adds JWT to requests
- ✅ Creates TokenAuthenticator for automatic token refresh
- ✅ Configures Retrofit with all interceptors
- ✅ Provides ApiServices to Repository
- ✅ Repository provided to ViewModels
- ✅ ViewModels used by UI screens

**Without ApiProvider:** Manual dependency creation would be needed everywhere.
**With ApiProvider:** All dependencies auto-injected via @Inject annotation.


