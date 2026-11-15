# CUSTOMER APP

## Scope
Android (Kotlin, Compose, Retrofit, Hilt) app for pharmacy CUSTOMERS only (role "user", account_type "user"). No admin features; admin-only endpoints are never implemented. Backend contract: `response.md` at the repo root (API v1, `/api/v1`).

## Networking rules
- ONE OkHttp stack: `AuthInterceptor`, `TokenAuthenticator`, `TokenManager`, `ApiProvider` (+ `VersionInterceptor`, `ErrorEventInterceptor`). Never add a second client, authenticator, token store or refresh flow.
- v1 logic activates only for paths containing `/api/v1/`; old endpoints keep working until the UI migration is finished.
- v1 Retrofits: `@V1Retrofit` (with authenticator) and `@V1AuthRetrofit` (without). Relative paths only, no leading slash, no `/api/v1` prefix.
- Repository (`repo/Repository.kt`) is the single data entry point: returns `Flow<ResultState<T>>`, errors carry `AppException(AppError)`. No Impl classes, no interface layer. Idempotency keys are always supplied by the caller.
- Public (no Bearer): auth/login, auth/verify-otp, auth/password-reset/*, `POST users`, app-config, openapi.json, health. auth/refresh uses the refresh token.

## Handoff files
- `app/src/main/java/com/saurabh/mediuserapp/data/v1/v1_contract.md` — signatures of core, error, network, DTOs, APIs.
- `app/src/main/java/com/saurabh/mediuserapp/data/v1/viewmodel_migration.md` — Repository methods, type reference, old-to-new call map, state rules, breakages.
- `app/src/main/java/com/saurabh/mediuserapp/data/v1/v1_audit.md` — pre-migration audit.
- `app_update_plan.md` — plan and repository status.

## Working rules
- Reuse first: extend existing classes, never create parallel versions.
- Never guess a field: take it from `response.md`; if unsure make it nullable and mark it unverified in the contract.
- Token discipline: grep names before reading, read line ranges, do not re-read files you wrote, skip build output, batch edits, keep replies short.
- Build check: `./gradlew :app:compileDebugKotlin -q 2>&1 | grep -E "^e: "`; tests: `./gradlew :app:testDebugUnitTest`.

## Features (done)

- Data layer on API v1: auth (login + OTP, refresh, logout, password reset), signup, profile (view/update), products (list, by id, barcode, categories), orders (create with idempotency key and optional prescription, list, get, cancel, prescription upload, invoice), devices, app-config, health.
- MyViewModel on the v1 Repository with AppError-aware states, paging (products, orders), app events (session invalid, blocked, rate limited, upgrade required) and role gate (only "user").

## Screens

- Existing: Splash, SignIn, SignUp, Otp, ForgotPassword (+Otp), ResetPassword, Product list, Profile, Waiting (see ui_followups.md); Orders and History screens are not yet bound to ViewModel data.
- Missing UI: order create/detail/cancel, invoice, prescription upload, upgrade/blocked screens, field-level form errors.

## Known issues

- Password-reset screens carry the email in the `userId` route argument.
- WaitingScreen cannot poll approval on v1 (no token before approval).
- Old `network/ApiServices.kt` and `network/response/*` remain on disk but are unused by the Repository.
