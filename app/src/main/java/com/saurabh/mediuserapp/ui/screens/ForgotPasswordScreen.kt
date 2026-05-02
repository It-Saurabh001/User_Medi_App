package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.AnimatedAuthContent
import com.saurabh.mediuserapp.ui.components.AuthButton
import com.saurabh.mediuserapp.ui.components.AuthHeader
import com.saurabh.mediuserapp.ui.components.AuthTextField
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

private const val TAG = "AuthScreen"

/**
 * Forgot Password — Step 1: Enter email to request a password-reset OTP.
 *
 * Flow: email → ViewModel.requestPasswordReset(email)
 *   Success → backend sends OTP to email & returns user_id
 *            → navigate to ForgotPasswordOtpRoutes(userId)
 */
@Composable
fun ForgotPasswordScreen(
    viewModel: MyViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val state by viewModel.passwordResetState.collectAsState()

    var email by remember { mutableStateOf("") }
    var formSubmitted by remember { mutableStateOf(false) }

    // Loading overlay
    LoadingOverlay(isVisible = state.isLoading)

    // Error → Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            Log.e(TAG, "ForgotPassword ERROR: $err")
            snackbarHostState.showSnackbar(message = err, duration = SnackbarDuration.Short)
        }
    }

    // Success → navigate to OTP screen for password reset
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                Log.d(TAG, "ForgotPassword SUCCESS ✅ message=${response.message}, user_id=${response.user_id}")
                Toast.makeText(context, "OTP sent to your email!", Toast.LENGTH_SHORT).show()
                response.user_id?.let { uid ->
                    Log.d(TAG, "Navigating to ForgotPasswordOtpRoutes with userId=$uid")
                    navController.navigate(Routes.ForgotPasswordOtpRoutes(userId = uid)) {
                        launchSingleTop = true
                    }
                } ?: Log.e(TAG, "ForgotPassword: user_id was null in response — cannot navigate")
            }
        }
    }

    // ----- UI -----

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            AnimatedAuthContent(delayMillis = 0) {
                AuthHeader(
                    title = "Forgot Password",
                    subtitle = "Enter your registered email to receive an OTP"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedAuthContent(delayMillis = 100) {
                    Text(
                        text = "We'll send a verification code to your email address so you can reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedAuthContent(delayMillis = 200) {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = formSubmitted && email.isBlank(),
                        errorMessage = if (formSubmitted && email.isBlank()) "Email is required" else null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedAuthContent(delayMillis = 300) {
                    AuthButton(
                        text = "Send OTP",
                        isLoading = state.isLoading,
                        onClick = {
                            formSubmitted = true
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }
                            Log.d(TAG, "ForgotPassword REQUEST — email=$email")
                            viewModel.requestPasswordReset(email)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedAuthContent(delayMillis = 350) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remember your password?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { navController.navigateUp() }) {
                            Text(
                                text = "Sign In",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
