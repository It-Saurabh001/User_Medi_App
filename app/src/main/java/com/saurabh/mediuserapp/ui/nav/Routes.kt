package com.saurabh.mediuserapp.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    // Startup & auth
    @Serializable
    object SplashRoutes

    @Serializable
    object SignInRoutes

    @Serializable
    object SignUpRoutes

    @Serializable
    data class VerifyOtpRoutes(val accountId: String)

    @Serializable
    data class WaitingRoutes(val email: String? = null)

    @Serializable
    object ForgotPasswordRoutes

    @Serializable
    data class ForgotPasswordOtpRoutes(val email: String)

    @Serializable
    data class ResetPasswordRoutes(val email: String, val otp: String)

    @Serializable
    object UpgradeRequiredRoutes

    @Serializable
    object AccountBlockedRoutes

    // Bottom bar tabs
    @Serializable
    object ProductRoutes

    @Serializable
    object OrdersRoutes

    @Serializable
    object HistoryRoutes

    @Serializable
    object ProfileRoutes

    // Home
    @Serializable
    data class SpecificProductRoutes(val productId: String)

    @Serializable
    object BarcodeScannerRoutes

    @Serializable
    data class PlaceOrderRoutes(val productId: String)

    @Serializable
    data class OrderPlacedRoutes(val orderId: String, val checkoutId: String? = null)

    // Cart / multi-item checkout
    @Serializable
    object CartRoutes

    @Serializable
    object CheckoutRoutes

    @Serializable
    data class CheckoutInvoiceRoutes(val checkoutId: String)

    // Orders
    @Serializable
    data class OrderDetailRoutes(val orderId: String)

    @Serializable
    data class UploadPrescriptionRoutes(val orderId: String)

    @Serializable
    data class PrescriptionViewerRoutes(val orderId: String)

    @Serializable
    data class InvoiceRoutes(val orderId: String)

    // Profile
    @Serializable
    object EditProfileRoutes

    @Serializable
    object ChangeCredentialsRoutes

    @Serializable
    object SettingsRoutes
}
