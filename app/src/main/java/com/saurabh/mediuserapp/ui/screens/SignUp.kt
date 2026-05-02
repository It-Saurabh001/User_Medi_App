package com.saurabh.mediuserapp.ui.screens

import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logAction
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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

private const val TAG = "AuthScreen"

/**
 * Modern Material 3 Signup screen.
 *
 * Collects: name, email, phone, password, confirm-password, address, pincode.
 * On success → navigates to SignIn screen (backend returns message, not OTP here).
 * Fully themed — works in Light & Dark mode.
 */
@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_UI, "SignUp Screen ENTERED composition") }
    SideEffect { AppLogger.v(AppLogger.TAG_UI, "SignUp Screen recomposed") }

    val context = LocalContext.current
    val state by viewModel.createUserState.collectAsState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    // Validation flags
    var passwordMismatch by remember { mutableStateOf(false) }
    var formSubmitted by remember { mutableStateOf(false) }

    // ----- Handle state changes -----

    // Loading overlay
    LoadingOverlay(isVisible = state.isLoading)

    // Error handling via Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            AppLogger.e(AppLogger.TAG_UI, "SignUp ERROR state received: $err")
            snackbarHostState.showSnackbar(
                message = err,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Success → navigate to Sign In, removing SignUp from back stack
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                Log.d(TAG, "SignUp SUCCESS ✅ message=${response.message}, status=${response.status}")
                Log.d(TAG, "Navigating to SignInRoutes — popping SignUpRoutes off back stack")
                Toast.makeText(context, "Account created! Please sign in.", Toast.LENGTH_LONG).show()
                navController.navigate(Routes.SignInRoutes) {
                    popUpTo(Routes.SignUpRoutes) { inclusive = true }
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
            // Gradient header
            AnimatedAuthContent(delayMillis = 0) {
                AuthHeader(
                    title = "Create Account",
                    subtitle = "Sign up to get started"
                )
            }

            // Form body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedAuthContent(delayMillis = 100) {
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        isError = formSubmitted && name.isBlank(),
                        errorMessage = if (formSubmitted && name.isBlank()) "Name is required" else null
                    )
                }

                AnimatedAuthContent(delayMillis = 150) {
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
                    AuthTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "+91 Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        isError = formSubmitted && phoneNumber.isBlank(),
                        errorMessage = if (formSubmitted && phoneNumber.isBlank()) "Phone number is required" else null
                    )
                }

                AnimatedAuthContent(delayMillis = 250) {
                    AuthPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
                        },
                        label = "Password",
                        isError = formSubmitted && password.isBlank(),
                        errorMessage = if (formSubmitted && password.isBlank()) "Password is required" else null
                    )
                }

                AnimatedAuthContent(delayMillis = 300) {
                    AuthPasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordMismatch = password.isNotEmpty() && it != password
                        },
                        label = "Confirm Password",
                        isError = passwordMismatch,
                        errorMessage = if (passwordMismatch) "Passwords do not match" else null
                    )
                }

                AnimatedAuthContent(delayMillis = 350) {
                    AuthTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address (optional)",
                        leadingIcon = Icons.Default.Home
                    )
                }

                AnimatedAuthContent(delayMillis = 400) {
                    AuthTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = "Pin Code (optional)",
                        leadingIcon = Icons.Default.LocationOn,
                        keyboardType = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sign Up button
                AnimatedAuthContent(delayMillis = 450) {
                    AuthButton(
                        text = "Sign Up",
                        isLoading = state.isLoading,
                        onClick = {
                            formSubmitted = true
                            Log.d(TAG, "SignUp CLICKED — name=$name, email=$email, phone=$phoneNumber")

                            // Validate
                            if (name.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
                                AppLogger.w(AppLogger.TAG_UI, "SignUp attempt with missing required fields")
                                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }
                            if (password != confirmPassword) {
                                AppLogger.w(AppLogger.TAG_UI, "SignUp attempt with password mismatch")
                                passwordMismatch = true
                                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }

                            AppLogger.i(AppLogger.TAG_UI, "SignUp REQUEST started for $email")
                            viewModel.createUser(
                                name = name,
                                password = password,
                                phoneNumber = phoneNumber,
                                email = email,
                                pinCode = pinCode,
                                address = address,
                                role = "user"
                            )
                        }
                    )
                }

                // Divider + navigation to Sign In
                AnimatedAuthContent(delayMillis = 500) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { navController.navigate(Routes.SignInRoutes) }) {
                                Text(
                                    text = "Sign In",
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
