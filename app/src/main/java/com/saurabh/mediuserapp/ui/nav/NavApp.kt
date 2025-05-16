package com.saurabh.mediuserapp.ui.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saurabh.mediuserapp.ui.screens.SignIn
import com.saurabh.mediuserapp.ui.screens.SignUp
import com.saurabh.mediuserapp.ui.screens.WaitingScreen
import com.saurabh.mediuserapp.viewModel.MyViewModel
@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SignUpRoutes
    ) {
        composable<Routes.SignUpRoutes> {
            SignUp(viewModel,navController)
        }
        composable<Routes.SignInRoutes> {
            SignIn(viewModel,navController)
        }
        composable <Routes.WaitingRoutes>{
            val data = it.toRoute<Routes.WaitingRoutes>()
            Log.d("TAG", "NavApp: userId = ${data.userId}")
            WaitingScreen(data.userId, viewModel)
        }
    }


}