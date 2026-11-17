# CUSTOMER APP

## Scope

Android (Kotlin, Compose, Retrofit, Hilt) app for pharmacy CUSTOMERS only (role "user", account_type "user"). No admin features; admin-only endpoints are never implemented. Backend contract: `response.md` at the repo root (API v1, `/api/v1`).

## Networking rules

- ONE OkHttp stack: `AuthInterceptor`, `TokenAuthenticator`, `TokenManager`, `ApiProvider` (+ `VersionInterceptor`, `ErrorEventInterceptor`). Never add a second client, authenticator, token store or refresh flow.
- v1 logic activates only for paths containing `/api/v1/`; old endpoints keep working until the UI migration is finished.
- v1 Retrofits: `@V1Retrofit` (with authenticator) and `@V1AuthRetrofit` (without). Relative paths only, no leading slash, no `/api/v1` prefix.
- Repository (`repo/Repository.kt`) is the single data entry point: returns `Flow<ResultState<T>>`, errors carry `AppException(AppError)`. No Impl classes, no interface layer. Idempotency keys are always supplied by the caller.
- Public (no Bearer): auth/login, auth/verify-otp, auth/password-reset/\*, `POST users`, app-config, openapi.json, health. auth/refresh uses the refresh token.

## Handoff files

- `app/src/main/java/com/saurabh/mediuserapp/data/v1/v1_contract.md` — signatures of core, error, network, DTOs, APIs.
- `app/src/main/java/com/saurabh/mediuserapp/data/v1/viewmodel_migration.md` — Repository methods, type reference, old-to-new call map, state rules, breakages.
- `app/src/main/java/com/saurabh/mediuserapp/data/v1/v1_audit.md` — pre-migration audit.
- `app_update_plan.md` — plan and repository status.
- `docs/ui_style_guide.md` — UI design system taken from MediAdminApp (components, layout, motion, per-page styling, blue palette proposal). Read before generating any UI.

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

# CLAUDE CODE AGENT PROMPT: MediUserApp Screens (4 per run)

> **Usage:** paste this whole file as the prompt. Change only the two lines below, then run once per part.
> `PART = 1`
> `ADMIN_APP_PATH = <path to MediAdminApp, e.g. ../MediAdminApp>`

---

## ROLE

You are a senior Android (Kotlin, Jetpack Compose, Material 3, Hilt, Navigation Compose type-safe routes) engineer working in **MediUserApp** (package `com.saurabh.mediuserapp`). Build exactly **4 screens per run** (the screens of `PART`), their shared components, the navigation for them, and any ViewModel state they need. Then stop. **Token discipline is a hard requirement.**

## SOURCES OF TRUTH (read each ONCE, never again)

Locate with one `glob **/{ui_rule,screens_and_navigation,color_guide,ui_style_guide,ui_handoff}.md`:

| File                        | Use                                                                                                                                                  |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui_rule.md`                | Strict UI rules (U1-U16). Obey them, except where PERMISSIONS below says otherwise.                                                                  |
| `color_guide.md`            | 44-colour blue guide. Only via `MaterialTheme.colorScheme.*` and `ui/theme` tokens (`StatusColors`, `Blue*`, ...).                                   |
| `ui_style_guide.md`         | Spacing, type, shapes, components, motion, keyboard rules.                                                                                           |
| `screens_and_navigation.md` | Screen specs, routes, APIs, back-stack rules, error handling, mermaid flow. Read **only the sections of this PART's screens** (grep headings first). |
| `ui_handoff.md`             | LOCKED files + `PENDING_EDGES` from earlier parts. Create it if missing.                                                                             |

**MediAdminApp (`<ADMIN_APP_PATH>`) is already analysed.** Treat its way of doing things as the house pattern: screen file structure, how state is collected and rendered (loading/empty/error), shared component naming and placement, form handling, error-code handling, navigation structure. Follow the same patterns here. It is **read-only**: never edit it.

## PERMISSIONS (override `ui_rule.md` U1/U2 for this task)

1. **Screens and components:** create the 4 screens of this PART. Create shared components in `ui/components/` whenever MediAdminApp has the equivalent pattern or 2+ screens need it. Do not create screens outside this PART.
2. **Navigation: full access, no restrictions.** Edit `Routes.kt`, `NavApp.kt` and any navigation file freely (routes, NavHost, start destination, back stack, transitions, bottom bar, global events 401/403/426) as needed so the flow in `screens_and_navigation.md` works.
3. **ViewModel / state / Repository / DTO / API:** the code is already up to date. If a screen needs state handling that is missing (new state class, flow, function, API call, DTO field), update them as needed. Keep changes to what the screens need, no unrelated refactors. Log every change in handoff.

Everything else in `ui_rule.md` stays strictly enforced.

## HARD RULES (violating any = failure)

- **Scope:** this PART's screens + their components + navigation + the ViewModel state they need + `docs/ui_handoff.md`. Do not restyle or rewrite unrelated screens.
- **No new dependencies, no gradle edits, no theme redesign.** If a required colour token is missing in `ui/theme`, add only that token and log it.
- **Colours:** zero raw hex or `Color.White/Black/Gray` outside `ui/theme`. Never copy colours/hex/theme/drawables from MediAdminApp. Green = success only. Logo colours only on Splash.
- **Touch:** `indication = null`, same-family darker shade on press/hold (110 ms), scale 0.97 only on standalone buttons/cards, never on list rows.
- **Type/shape:** only `MaterialTheme.typography/shapes`; font scale 1.5x safe (`heightIn`, `maxLines`, ellipsis).
- **Layout:** `Scaffold`, 16 dp margins, 48 dp touch targets, one primary action per screen, loading/empty/error(Retry) on every data screen, `imePadding` + scroll + tap-outside-to-dismiss-keyboard on forms.
- Destructive actions go through `AlertDialog` with Red 600 confirm.
- A screen that already exists and complies stays untouched. Existing screens: **edit in place, smallest diff** (colour/state fixes listed in `screens_and_navigation.md` section 8.3).
- No comment/KDoc noise, no unit tests, no README, no formatting of untouched code.

## TOKEN BUDGET RULES

- No preamble, narration, plans, summaries or file dumps in chat. Think silently.
- `glob`/`grep` first; read a file once, with `view_range` when large. **Never read `MyViewModel.kt` fully:** grep `val .*State`, `fun <name>`, then view only those lines.
- Batch independent reads/greps in one tool call; write several files per call.
- Skip `build/`, `.gradle/`, generated code, and every file marked LOCKED in handoff (use only signatures listed there).
- Open a MediAdminApp file only when you need its exact structure for a screen of this PART. No screenshots, no running the app.
- Run `./gradlew :app:compileDebugKotlin` **once** at the end; fix only errors in files you touched; re-run only if it failed.
- **Final chat reply = exactly one line:**
  `Part N done. Files changed: N. Build: OK|FAIL. Missing: <list or none>.`

## WORKFLOW (follow in order)

1. **Read once:** the 5 md files (scoped as above), `ui/theme/*` (token names via grep `val `), and `fun ` signatures in `ui/components/` and `ui/nav/`.
2. **Resolve each of the 4 screens** (grep by name and by old names):
   - a) Exists in MediUserApp and complies, leave untouched. Exists but non-compliant: fix in place (colours via tokens, states, keyboard, motion, section 8.3 fixes).
   - b) Missing here but a matching screen exists in MediAdminApp: **port** it (PORTING RULES).
   - c) Exists nowhere: write it fresh from the spec, using the admin app's patterns.
3. **Components:** create the shared components the PART needs in `ui/components/`, following admin's naming/placement. List signatures in handoff.
4. **ViewModel/state:** grep what the PART needs (states, functions, `Idempotency-Key`, multipart parts). Update or add as needed.
5. **Navigation:** add `@Serializable` routes and `composable<...>` destinations for the 4 screens, connect them from earlier screens' callbacks, apply back-stack rules from the flow doc, resolve any `PENDING_EDGES` that now have both ends. Edges whose target belongs to a later part cannot be wired yet: append them to `PENDING_EDGES` (no stubs).
   - Part 1: set NavHost-level transitions once per `ui_rule.md` Part 3 section 2 (forward right-to-left, back left-to-right, bottom tabs crossfade, sheets bottom-to-top, dialogs centre scale).
6. **Compile once**, fix, done.
7. **Handoff:** append (never rewrite) to `docs/ui_handoff.md` in the U16 format, plus:
   `LOCKED: <files>` | `Signatures: <new components/routes>` | `ViewModel changes: <list or none>` | `Navigation changes: <list>` | `PENDING_EDGES: A->B, ...` | `Theme additions: <list or none>`.

## PORTING RULES (MediAdminApp to MediUserApp)

Copy structure and logic, then adapt:

1. Package/imports to `com.saurabh.mediuserapp`; use **this** project's ViewModel state names, `ResultState/UiState`, routes and components.
2. Strip admin-only elements (admin buttons, `purchase_price`, batches, user management, audit, admin profile logic).
3. Replace all colours/shapes/type with this project's tokens per `color_guide.md`. Carry over no hex, brush, drawable or theme reference.
4. Customer contract: `account_type = "user"`; OTP success requires `role == "user"` else back to SignIn; user endpoints only; handle backend codes (`ACCOUNT_NOT_APPROVED` to Waiting, `ACCOUNT_BLOCKED` to AccountBlocked, `UPGRADE_REQUIRED` to Upgrade, `OUT_OF_STOCK`, `PRESCRIPTION_REQUIRED`, `INVALID_TRANSITION`).
5. Remove anything unused after stripping. Keep the result compilable.

Likely admin sources (grep real names): SignIn<-Login, Otp<-Otp, ForgotPassword<-ForgotPassword, ResetPassword<-ResetPassword, Splash<-Splash, UpgradeRequired<-UpgradeRequired, AccountBlocked<-AccountBlocked, ProductScreen<-ProductList, ProductDetail<-ProductDetail, BarcodeScanner<-BarcodeScanner, OrdersScreen<-OrderList, OrderDetail<-OrderDetail (customer actions only), PrescriptionViewer<-PrescriptionViewer, Invoice<-Invoice, ProfileScreen/EditProfile<-AdminProfile, SettingsAbout<-Settings/About.

## PART PLAN (do only the screens of `PART`)

| PART  | Screens (4)                                                                                                                                | Key points beyond the spec files                                                                                                                                                                                                                                                                                                                                                                                           |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **1** | `SplashScreen`, `SignIn`, `SignUp`, `OtpScreen`                                                                                            | `GET /app-config` + min-version + retry on Splash; SignIn `account_type=user`, 403 branches; SignUp client validation (phone `^\+?[0-9]{7,15}$`, pincode `^[0-9]{4,10}$`, password min 8, confirm match), optional photo, backend `fields` under inputs; Otp 6 digits, 300 s expiry, resend = login again, 5 wrong tries to SignIn, device register after success, `popUpTo(0)` to Products. Also set NavHost transitions. |
| **2** | `ForgotPasswordScreen`, `ForgotPasswordOtpScreen`, `ResetPasswordScreen`, `WaitingScreen`                                                  | No separate reset-OTP verify API: OtpScreen only collects OTP and passes it on; confirm sends `email+otp+new_password`; invalid OTP returns to ForgotPasswordOtp; Waiting has "Check again" (re-login) and back to SignIn.                                                                                                                                                                                                 |
| **3** | `UpgradeRequiredScreen`, `AccountBlockedScreen`, `ProductScreen`, `ProductDetailScreen` (+ `ProductFilterSheet`)                           | Back disabled on Upgrade/Blocked; wire global events (401 invalid/refresh fail to SignIn clearing stack, 403 blocked, 426) in `NavApp`; ProductScreen search, category chips, filter sheet, barcode icon, pagination, pull-to-refresh, `StatusChip`, 1:1 `ContentScale.Fit` image with category fallback icon; ignore `purchase_price`; Buy button to PlaceOrder (pending edge).                                           |
| **4** | `BarcodeScannerScreen`, `PlaceOrderScreen`, `OrderPlacedScreen`, `OrdersScreen` (+ `PrescriptionSourceSheet`)                              | One order = one product, no cart; Rx section when `requires_prescription`; multipart `prescription`; file type/size check from app-config; `Idempotency-Key` generated per attempt and reused on retry; `OUT_OF_STOCK` shows `fields.quantity`; `OrderPlaced` pops PlaceOrder; Orders status chips All/Pending/Approved/Cancelled with load-more.                                                                          |
| **5** | `OrderDetailScreen` (+ `EditOrderInfoSheet`, `CancelOrderDialog`), `UploadPrescriptionScreen`, `PrescriptionViewerScreen`, `InvoiceScreen` | Buttons by status exactly as in flow doc (pending: edit/upload/cancel; approved: read-only; sold/returned: invoice; cancelled: reason); `INVALID_TRANSITION` reloads order; replace old `EachUserOrderRoutes(userId)` with `OrderDetailRoutes(orderId)`.                                                                                                                                                                   |
| **6** | `HistoryScreen`, `ProfileScreen`, `EditProfileScreen`, `ChangeCredentialsScreen`                                                           | History = `GET /orders?status=sold                                                                                                                                                                                                                                                                                                                                                                                         | returned` (not sell-history API), date filter; Profile buttons + logout dialog (`POST /auth/logout`, unregister device, clear stack); EditProfile multipart `image`; ChangeCredentials needs `current_password`. |
| **7** | `SettingsAboutScreen` + **final navigation audit**                                                                                         | Audit every edge of the mermaid flow in `screens_and_navigation.md`: no orphan routes, no unresolved `PENDING_EDGES`, back-stack rules, bottom-tab crossfade, system back on Upgrade/Blocked disabled. Fix navigation gaps.                                                                                                                                                                                                |

If the PART's screens are already finished per handoff, skip them and note it; never redo LOCKED work.

## DEFINITION OF DONE

- The 4 screens compile, follow `ui_rule.md` and `color_guide.md`, follow MediAdminApp's patterns, use correct ViewModel state, and are reachable via navigation per the flow doc.
- No raw colours outside `ui/theme`; no admin colours/hex/theme copied.
- Handoff appended; touched files marked LOCKED.
- One-line final reply, nothing else.
