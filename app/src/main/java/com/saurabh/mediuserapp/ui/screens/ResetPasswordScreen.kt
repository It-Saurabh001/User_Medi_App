package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.AnimatedAuthContent
import com.saurabh.mediuserapp.ui.components.AuthButton
import com.saurabh.mediuserapp.ui.components.AuthHeader
import com.saurabh.mediuserapp.ui.components.AuthPasswordField
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

private const val TAG = "AuthScreen"

/**
 * Reset Password — Step 3 (final step) of the forgot-password flow.
 *
 * User enters:  OTP (from email) + New Password + Confirm New Password
 * Calls: viewModel.resetPasswordOtp(userId, otp, newPassword)
 *
 * On success → navigate back to SignIn.
 *
 * @param userId from PasswordResetResponse
 */
@Composable
fun ResetPasswordScreen(
    userId: String,
    viewModel: MyViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val state by viewModel.passwordResetOtpState.collectAsState()

    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var formSubmitted by remember { mutableStateOf(false) }
    var passwordMismatch by remember { mutableStateOf(false) }

    // Loading overlay
    LoadingOverlay(isVisible = state.isLoading)

    // Error → Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            Log.e(TAG, "ResetPassword ERROR: $err")
            snackbarHostState.showSnackbar(message = err, duration = SnackbarDuration.Short)
        }
    }

    // Success → navigate to Sign In, clear entire forgot-password flow from back stack
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                Log.d(TAG, "ResetPassword SUCCESS ✅ message=${response.message}, status=${response.status}")
                Log.d(TAG, "Clearing forgot-password back stack — navigating to SignInRoutes")
                Toast.makeText(context, "Password reset! Please sign in with your new password.", Toast.LENGTH_LONG).show()
                navController.navigate(Routes.SignInRoutes) {
                    // Pop everything back to (and including) ForgotPasswordRoutes
                    popUpTo(Routes.ForgotPasswordRoutes) { inclusive = true }
                    launchSingleTop = true
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
            AnimatedAuthContent(delayMillis = 0) {
                AuthHeader(
                    title = "Reset Password",
                    subtitle = "Create a new password for your account"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // OTP field
                AnimatedAuthContent(delayMillis = 100) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        label = { Text("Enter OTP") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "OTP",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = formSubmitted && otp.length != 6,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    if (formSubmitted && otp.length != 6) {
                        Text(
                            text = "Please enter 6-digit OTP",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        )
                    }
                }

                // New password
                AnimatedAuthContent(delayMillis = 200) {
                    AuthPasswordField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            passwordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
                        },
                        label = "New Password",
                        isError = formSubmitted && newPassword.isBlank(),
                        errorMessage = if (formSubmitted && newPassword.isBlank()) "Password is required" else null
                    )
                }

                // Confirm new password
                AnimatedAuthContent(delayMillis = 300) {
                    AuthPasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordMismatch = newPassword.isNotEmpty() && it != newPassword
                        },
                        label = "Confirm New Password",
                        isError = passwordMismatch,
                        errorMessage = if (passwordMismatch) "Passwords do not match" else null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reset button
                AnimatedAuthContent(delayMillis = 400) {
                    AuthButton(
                        text = "Reset Password",
                        isLoading = state.isLoading,
                        onClick = {
                            formSubmitted = true

                            if (otp.length != 6) {
                                Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }
                            if (newPassword.isBlank()) {
                                Toast.makeText(context, "Please enter new password", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }
                            if (newPassword != confirmPassword) {
                                passwordMismatch = true
                                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }

                            Log.d(TAG, "ResetPassword REQUEST — userId=$userId, otp=$otp")
                            viewModel.resetPasswordOtp(userId = userId, otp = otp, newPassword = newPassword)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedAuthContent(delayMillis = 450) {
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
                        TextButton(onClick = {
                            navController.navigate(Routes.SignInRoutes) {
                                popUpTo(Routes.SignInRoutes) { inclusive = true }
                            }
                        }) {
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
