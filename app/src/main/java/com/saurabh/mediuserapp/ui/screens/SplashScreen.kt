package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.R
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.ui.components.ShadeButton
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Blue200
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val upgradeRequired by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val configError = (config as? UiState.Error)?.error?.userMessage()

    LaunchedEffect(isLoggedIn, upgradeRequired, config) {
        if (config is UiState.Loading && upgradeRequired == null) return@LaunchedEffect
        if (configError != null && upgradeRequired == null) return@LaunchedEffect
        if (upgradeRequired != null) return@LaunchedEffect
        delay(1200)
        navController.navigate(if (isLoggedIn) Routes.ProductRoutes else Routes.SignInRoutes) {
            popUpTo(Routes.SplashRoutes) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.glossy_blue_healthcare_shield_icon),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            when {
                upgradeRequired != null -> SplashMessage(
                    "This version of the app is no longer supported. Please update to continue."
                )
                configError != null -> {
                    SplashMessage(configError)
                    Spacer(modifier = Modifier.height(16.dp))
                    ShadeButton("Retry", { viewModel.getAppConfig() })
                }
                else -> LinearProgressIndicator(
                    modifier = Modifier.width(120.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Blue200
                )
            }
        }
    }
}

@Composable
private fun SplashMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.widthIn(max = 360.dp)
    )
}
