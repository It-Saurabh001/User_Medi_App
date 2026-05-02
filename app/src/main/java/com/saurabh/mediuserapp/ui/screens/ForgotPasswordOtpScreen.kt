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

private const val TAG = "AuthScreen"

/**
 * Forgot Password — Step 2: Enter the OTP received via email.
 *
 * This screen does NOT call verifyUserOtp (that's for login).
 * Instead, on successful OTP entry, it navigates to the ResetPassword screen
 * where the user enters OTP + new password together for resetPasswordOtp().
 *
 * @param userId from PasswordResetResponse
 */
@Composable
fun ForgotPasswordOtpScreen(
    userId: String,
    viewModel: MyViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    var otp by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        isResendEnabled = true
    }

    // ----- UI -----

    Scaffold(
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
                    title = "Enter OTP",
                    subtitle = "We sent a verification code to your email"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User-ID chip
                AnimatedAuthContent(delayMillis = 100) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "User: $userId",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // OTP field
                AnimatedAuthContent(delayMillis = 200) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Timer
                AnimatedAuthContent(delayMillis = 250) {
                    Text(
                        text = if (isResendEnabled) "Didn't receive code?" else "Resend code in ${timeLeft}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Next button → go to Reset Password screen with OTP
                AnimatedAuthContent(delayMillis = 300) {
                    AuthButton(
                        text = "Continue",
                        onClick = {
                            if (otp.length == 6) {
                                Log.d(TAG, "ForgotPasswordOtp — OTP entered: $otp, userId=$userId → navigating to ResetPassword")
                                // We pass the OTP via savedStateHandle or as part of the route.
                                // Here we navigate to ResetPassword and pass userId; OTP is re-entered there
                                // OR we could store OTP temporarily. For simplicity we navigate and let user
                                // enter OTP + new password in the next screen.
                                navController.navigate(Routes.ResetPasswordRoutes(userId = userId))
                            } else {
                                Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Resend button
                AnimatedAuthContent(delayMillis = 350) {
                    TextButton(
                        onClick = {
                            if (isResendEnabled) {
                                Log.d(TAG, "Resend password-reset OTP for userId=$userId")
                                // Re-request OTP — the email is not available here, so just show toast
                                Toast.makeText(context, "Please go back and re-enter your email to resend OTP", Toast.LENGTH_LONG).show()
                                isResendEnabled = false
                                timeLeft = 60
                            }
                        },
                        enabled = isResendEnabled
                    ) {
                        Text(
                            text = "Resend OTP",
                            color = if (isResendEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedAuthContent(delayMillis = 400) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wrong email?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { navController.navigateUp() }) {
                            Text(
                                text = "Go Back",
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
