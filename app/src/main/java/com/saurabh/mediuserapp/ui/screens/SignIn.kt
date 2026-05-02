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
import com.saurabh.mediuserapp.ui.components.AuthPasswordField
import com.saurabh.mediuserapp.ui.components.AuthTextField
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logAction

private const val TAG = "AuthScreen"

/**
 * Modern Material 3 Login screen.
 *
 * Flow: email + password → ViewModel.loginUser()
 *   ├─ Loading → LoadingOverlay dialog
 *   ├─ Success → navigate to OTP verification (VerifyOtpRoutes)
 *   └─ Error → Snackbar
 *
 * Also contains "Forgot Password?" link → ForgotPasswordRoutes
 * and "Sign Up" link → SignUpRoutes
 */
@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_UI, "SignIn Screen ENTERED composition") }
    SideEffect { AppLogger.v(AppLogger.TAG_UI, "SignIn Screen recomposed") }

    val context = LocalContext.current
    val state by viewModel.loginUserState.collectAsState()

    // Form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formSubmitted by remember { mutableStateOf(false) }

    // Loading overlay
    LoadingOverlay(isVisible = state.isLoading)

    // Error → Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            AppLogger.e(AppLogger.TAG_UI, "Login ERROR state received: $err")
            snackbarHostState.showSnackbar(message = err, duration = SnackbarDuration.Short)
        }
    }

    // Success → navigate to OTP verification
    // Guard flag prevents double-navigation on recomposition
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                Log.d(TAG, "Login SUCCESS: user_id=${response.user_id}, role=${response.role}, message=${response.message}")
                Log.d(TAG, "Navigating to VerifyOtpRoutes — popping SignIn off back stack")
                Toast.makeText(context, "OTP sent! Please verify.", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.VerifyOtpRoutes(userId = response.user_id)) {
                    // Pop SignIn so back-press on OTP screen doesn't return here
                    popUpTo(Routes.SignInRoutes) { inclusive = true }
                }
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
            // Gradient header
            AnimatedAuthContent(delayMillis = 0) {
                AuthHeader(
                    title = "Welcome Back",
                    subtitle = "Sign in to continue"
                )
            }

            // Form body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedAuthContent(delayMillis = 100) {
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

                AnimatedAuthContent(delayMillis = 200) {
                    AuthPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isError = formSubmitted && password.isBlank(),
                        errorMessage = if (formSubmitted && password.isBlank()) "Password is required" else null
                    )
                }

                // Forgot password link
                AnimatedAuthContent(delayMillis = 250) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            Log.d(TAG, "Navigating to ForgotPasswordRoutes")
                            navController.navigate(Routes.ForgotPasswordRoutes)
                        }) {
                            Text(
                                text = "Forgot Password?",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sign In button
                AnimatedAuthContent(delayMillis = 300) {
                    AuthButton(
                        text = "Sign In",
                        isLoading = state.isLoading,
                        onClick = {
                            formSubmitted = true
                            AppLogger.d(AppLogger.TAG_UI, "Sign In Button CLICKED").logAction("User Interaction")

                            if (email.isBlank() || password.isBlank()) {
                                AppLogger.w(AppLogger.TAG_UI, "Login attempt with empty fields")
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }

                            AppLogger.i(AppLogger.TAG_UI, "Login REQUEST started for $email")
                            viewModel.loginUser(email, password)
                        }
                    )
                }

                // Divider + sign up
                AnimatedAuthContent(delayMillis = 400) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Don't have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                navController.navigate(Routes.SignUpRoutes)
                            }) {
                                Text(
                                    text = "Sign Up",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}