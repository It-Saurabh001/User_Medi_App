package com.saurabh.mediuserapp.ui.screens


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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediuserapp.ui.nav.Routes
import kotlinx.coroutines.delay

@Preview
@Composable
private fun lkfhslf() {
    SplashScreen(navController = rememberNavController())
    
}

@Composable
fun SplashScreen(navController: NavController) {
    // Side-effect for delay
    LaunchedEffect(Unit) {
        delay(2000) // 2 sec delay
        navController.navigate(Routes.SignInRoutes.route) {
            popUpTo("splash") { inclusive = true }
        }
    }
    Scaffold(
        containerColor = Color.White
    ) {
        innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)
            .fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ){
            Image(imageVector = Icons.Default.Person,
                modifier = Modifier
                    .fillMaxWidth(0.8f) // screen width का 80%
                    .aspectRatio(1f),   // square बनाए रखेगा
                contentScale = ContentScale.Fit,
                 contentDescription = "Splash Image")

        }
    }
    
}