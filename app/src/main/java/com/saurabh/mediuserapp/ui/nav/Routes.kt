package com.saurabh.mediuserapp.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {

    @Serializable
    object SignInRoutes {
        const val route = "SignInRoutes"
        operator fun invoke() = route
    }
    @Serializable
    object HomeRoutes {
        const val route = "HomeRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object SplashRoutes {
        const val route = "SplashRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object HistoryRoutes {
        const val route = "HistoryRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object SignUpRoutes {
        const val route = "SignupRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ProfileRoutes {
        const val route = "ProfileRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object OtpRoutes {
        const val route = "OtpRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object OtpRequestRoutes {
        const val route = "OtpRequestRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ProductRoutes {
        const val route = "productroutes"
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
    data class WaitingRoutes(val userId: String?) {
        companion object {
            const val route = "WaitingRoutes/{userId}"
            operator fun invoke(userId: String) = "WaitingRoutes/$userId"
        }
    }


}

