package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    Log.d("SplashScreen", "SplashScreen Launched - Checking Authentication...")

    // Navigation after delay
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay

        val isLoggedIn = viewModel.isUserLoggedIn.value
        Log.d("SplashScreen", "isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            Log.d("SplashScreen", "Token found - Navigating to ProductRoutes")
            navController.navigate(Routes.ProductRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            Log.d("SplashScreen", "No token found - Navigating to SignInRoutes")
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.SplashRoutes) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Animated gradient background (same as SignIn/SignUp)
    val infiniteTransition = rememberInfiniteTransition(label = "splashGradient")
    val animatedFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    val animatedBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A2A6C), // deep indigo
            Color(0xFFB21F1F), // rich crimson
            Color(0xFFFDBB5D)  // warm amber
        ),
        startY = animatedFraction * 0.5f,
        endY = 0.5f + animatedFraction * 0.5f
    )

    // Pulsing scale animation for the logo
    val infiniteTransitionLogo = rememberInfiniteTransition(label = "logoPulse")
    val logoScale by infiniteTransitionLogo.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Glass card containing the medical logo and text
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(48.dp),
                        spotColor = Color(0x33000000),
                        ambientColor = Color(0x22000000)
                    )
                    .clip(RoundedCornerShape(48.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.92f),
                                Color.White.copy(alpha = 0.85f)
                            )
                        )
                    ),
                shape = RoundedCornerShape(48.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated medical icon
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Medical Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .scale(logoScale),
                        tint = Color(0xFFB21F1F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // App Name with gradient text effect
                    Text(
                        text = "MediUser",
                        fontSize = 32.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF1A2A6C),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Your Health Companion",
                        fontSize = 16.sp,
                        color = Color(0xFF5A5A89)
                    )
                }
            }
        }
    }
}