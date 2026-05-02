package com.saurabh.mediuserapp.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    @Serializable
    object SignUpRoutes

    @Serializable
    object SignInRoutes

    @Serializable
    object ProductRoutes

    @Serializable
    object SplashRoutes
    @Serializable
    object HistoryRoutes {
        const val route = "HistoryRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ProfileRoutes {
        const val route = "ProfileRoutes"
        operator fun invoke() = route
    }



    @Serializable
    object OrdersRoutes {
        const val route = "ordersRoutes"
        operator fun invoke() = route
    }

    @Serializable
    class SpecificProductRoutes(val productId: String) {
        companion object {
            const val route = "specificProductRoutes/{productId}"
            operator fun invoke(productId: String) = "SpecificProductRoutes/$productId"
        }
    }

    @Serializable
    class EachUserOrderRoutes(val userId: String) {
        companion object {
            const val route = "orderDetailRoutes/{userId}"
            operator fun invoke(userId: String) = "orderDetailRoutes/$userId"
        }
    }

    @Serializable
    object OtpRoutes

    @Serializable
    data class WaitingRoutes(val userId: String?) {
        companion object {
            const val route = "WaitingRoutes/{userId}"
            operator fun invoke(userId: String) = "WaitingRoutes/$userId"
        }
    }
    @Serializable
    data class VerifyOtpRoutes(val userId: String?)

    // ===========================
    // NEW AUTH ROUTES
    // ===========================

    /** OTP screen for forgot-password flow — carries userId from PasswordResetResponse */
    @Serializable
    data class ForgotPasswordOtpRoutes(val userId: String)

    /** Reset-password screen — carries userId so we can call resetPasswordOtp */
    @Serializable
    data class ResetPasswordRoutes(val userId: String)

    /** Forgot-password entry screen (enter email) */
    @Serializable
    object ForgotPasswordRoutes
}
