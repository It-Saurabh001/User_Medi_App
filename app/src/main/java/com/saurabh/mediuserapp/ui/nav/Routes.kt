package com.saurabh.mediuserapp.ui.nav

import kotlinx.serialization.Serializable

sealed class Routes {

    @Serializable
    object SignUpRoutes

    @Serializable
    object SignInRoutes

    @Serializable
    data class WaitingRoutes(val userId : String)



}