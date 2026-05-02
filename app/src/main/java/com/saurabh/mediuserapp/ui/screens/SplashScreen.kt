package com.saurabh.mediuserapp.ui.screens


import android.util.Log
import android.window.SplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.MediUserAppTheme
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    Log.d("SplashScreen", "SplashScreen Launched - Checking Authentication...")

    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay

        // Check if user is logged in using TokenManager
        val isLoggedIn = viewModel.isUserLoggedIn.value

        Log.d("SplashScreen", "isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            // User has valid token → Navigate to main app
            Log.d("SplashScreen", "Token found - Navigating to ProductRoutes")
            navController.navigate(Routes.ProductRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // No token → Navigate to login
            Log.d("SplashScreen", "No token found - Navigating to SignInRoutes")
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = Icons.Default.Person,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                contentDescription = "Splash Image"
            )
        }
    }
}

// Note: Preview removed because SplashScreen requires NavController and ViewModel
// To properly test, run the app or create a test NavController
