package com.saurabh.mediuserapp.ui.screens

import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logAction
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.AnimatedAuthContent
import com.saurabh.mediuserapp.ui.components.AuthButton
import com.saurabh.mediuserapp.ui.components.AuthHeader
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

private const val OTP_TAG = "OtpScreen"

/**
 * OTP Verification Screen — used after login.
 *
 * The backend sends an OTP to the user's registered contact after login.
 * This screen verifies it via [MyViewModel.verifyUserOtp].
 *
 * Navigation on success:
 *   → ProductRoutes with popUpTo(0) { inclusive = true }
 *   This clears the ENTIRE back stack so the user can NEVER back-press to auth screens.
 *
 * @param userId  Received from [LoginUserResponse.user_id]
 */
@Composable
fun OtpScreen(
    userId: String,
    viewModel: MyViewModel,
    navController: NavHostController
) {
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_UI, "OtpScreen ENTERED composition") }
    SideEffect { AppLogger.v(AppLogger.TAG_UI, "OtpScreen recomposed") }

    val context = LocalContext.current
    val state by viewModel.verifyOtpState.collectAsState()

    var otp by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }
    // Guard flag — prevents duplicate navigation on recomposition
    var navigated by remember { mutableStateOf(false) }

    Log.d(OTP_TAG, "OtpScreen CREATED — userId=$userId")

    // ── State handling ─────────────────────────────────────────────────────

    LoadingOverlay(isVisible = state.isLoading)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            AppLogger.e(AppLogger.TAG_UI, "VerifyOTP ERROR state received: $err")
            snackbarHostState.showSnackbar(message = err, duration = SnackbarDuration.Long)
        }
    }

    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                Log.d(OTP_TAG, "VerifyOTP SUCCESS ✅")
                Log.d(OTP_TAG, "  access_token : ${response.access_token}")
                Log.d(OTP_TAG, "  refresh_token: ${response.refresh_token}")
                Log.d(OTP_TAG, "  role         : ${response.role}")
                Log.d(OTP_TAG, "  message      : ${response.message}")
                Log.d(OTP_TAG, "Tokens saved — navigating to ProductRoutes (clearing entire back stack)")

                Toast.makeText(context, "Verified! Welcome 🎉", Toast.LENGTH_SHORT).show()

                navController.navigate(Routes.ProductRoutes) {
                    // ✅ KEY: popUpTo(0) clears EVERY destination — auth screens gone
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // ── Countdown timer for Resend button ──────────────────────────────────
    LaunchedEffect(Unit) {
        Log.d(OTP_TAG, "Countdown timer started for userId=$userId")
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        isResendEnabled = true
        Log.d(OTP_TAG, "Countdown finished — Resend button enabled")
    }

    // ── UI ─────────────────────────────────────────────────────────────────

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
                    title = "Verify OTP",
                    subtitle = "Enter the 6-digit code sent to your registered contact"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User-ID info chip (helpful for debugging)
                AnimatedAuthContent(delayMillis = 100) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Session ID: $userId",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // OTP input field
                AnimatedAuthContent(delayMillis = 200) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it },
                        label = { Text("Enter 6-digit OTP") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "OTP",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Timer / resend hint
                AnimatedAuthContent(delayMillis = 250) {
                    Text(
                        text = if (isResendEnabled) "Didn't receive code? Tap Resend"
                               else "Resend available in ${timeLeft}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Verify button
                AnimatedAuthContent(delayMillis = 300) {
                    AuthButton(
                        text = "Verify OTP",
                        isLoading = state.isLoading,
                        enabled = otp.length == 6,
                        onClick = {
                            Log.d(OTP_TAG, "Verify tapped — userId=$userId, otp=$otp (length=${otp.length})")
                            if (otp.length == 6) {
                                Log.d(OTP_TAG, "Calling viewModel.verifyUserOtp()")
                                viewModel.verifyUserOtp(userId = userId, otp = otp)
                            } else {
                                Toast.makeText(context, "Enter all 6 digits", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Resend button
                AnimatedAuthContent(delayMillis = 350) {
                    OutlinedButton(
                        onClick = {
                            Log.d(OTP_TAG, "Resend OTP tapped for userId=$userId")
                            Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                            isResendEnabled = false
                            timeLeft = 60
                        },
                        enabled = isResendEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Resend OTP",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Back to Sign In
                AnimatedAuthContent(delayMillis = 400) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wrong account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            Log.d(OTP_TAG, "Go back tapped — navigating up")
                            navController.navigate(Routes.SignInRoutes) {
                                popUpTo(Routes.SignInRoutes) { inclusive = true }
                            }
                        }) {
                            Text(
                                text = "Sign In Again",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}