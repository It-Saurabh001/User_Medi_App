# v1 audit (Part 1, pre-migration)

- TokenManager: EXISTS (network/TokenManager.kt), plain SharedPreferences, no account_id/type/expiry -> updated additively
- TokenAuthenticator: EXISTS (network/TokenAuthenticator.kt), own lock, legacy refresh only -> v1 branch added
- AuthInterceptor: EXISTS (network/AuthInterceptor.kt), legacy excluded paths -> v1 branch added
- ApiProvider (Hilt network module): EXISTS (network/ApiProvider.kt) -> @V1Retrofit/@V1AuthRetrofit added
- Base URL: EXISTS (BuildConfig.WirelessPhysicalDevice via ApiProvider.BASE_URL1; unused utils/const.kt BASE_URL1)
- Logging: EXISTS (HttpLoggingInterceptor in ApiProvider + network/LoggingInterceptor.kt) -> redaction added
- ResultState: EXISTS (common/ResultState.kt, legacy, untouched)
- ApiResult, AppError, ErrorCode, UiState: MISSING -> created in data.v1.core / data.v1.error
- PageRequest, Pagination, ApiEnvelope/PagedEnvelope: MISSING -> created in data.v1.core
- VersionInterceptor, ErrorEventInterceptor, AppEvents, IdempotencyKeyProvider: MISSING -> created
- v1 source of truth: response.md at repo root (app/.../response.md is the legacy API doc)
