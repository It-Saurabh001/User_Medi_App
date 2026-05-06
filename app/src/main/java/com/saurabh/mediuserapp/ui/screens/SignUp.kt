package com.saurabh.mediuserapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import com.saurabh.mediuserapp.utils.AppLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_UI, "SignUp Screen ENTERED") }

    val context = LocalContext.current
    val state by viewModel.createUserState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Form fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordMismatch by remember { mutableStateOf(false) }
    var formSubmitted by remember { mutableStateOf(false) }

    // Focus order
    val emailFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }
    val addressFocus = remember { FocusRequester() }
    val pinFocus = remember { FocusRequester() }

    LoadingOverlay(isVisible = state.isLoading)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(
                message = err,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
    }

    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let {
            if (!navigated) {
                navigated = true
                Toast.makeText(context, "Account created! Please sign in.", Toast.LENGTH_LONG).show()
                navController.navigate(Routes.SignInRoutes) {
                    popUpTo(Routes.SignUpRoutes) { inclusive = true }
                }
            }
        }
    }

    // ✓ Soft pastel gradient (exactly as in your reference image)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFC9E9FF), // light ice blue
            Color(0xFFB0D4FF), // soft blue
            Color(0xFFE0D4FF)  // lavender
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Branding (dark indigo, as in your image)
                Text(
                    text = "MediUser",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2A6C),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Create your account",
                    fontSize = 16.sp,
                    color = Color(0xFF5A5A89)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Glossy text field colors (semi‑transparent white background)
                val glossyColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A2A6C),
                    unfocusedBorderColor = Color(0xFFD0D0E0),
                    focusedTextColor = Color(0xFF1A2A6C),
                    unfocusedTextColor = Color(0xFF3A3A6C),
                    cursorColor = Color(0xFF1A2A6C),
                    focusedLabelColor = Color(0xFF1A2A6C),
                    unfocusedLabelColor = Color(0xFF5A5A89),
                    focusedContainerColor = Color.White.copy(alpha = 0.92f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f)
                )

                // Full Name
                GlossyTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    isError = formSubmitted && name.isBlank(),
                    errorMessage = if (formSubmitted && name.isBlank()) "Name required" else null,
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { emailFocus.requestFocus() }
                )

                // Email
                GlossyTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    isError = formSubmitted && email.isBlank(),
                    errorMessage = if (formSubmitted && email.isBlank()) "Email required" else null,
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { phoneFocus.requestFocus() },
                    modifier = Modifier.focusRequester(emailFocus),
                    keyboardType = KeyboardType.Email
                )

                // Phone Number
                GlossyTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "+91 Phone Number",
                    leadingIcon = Icons.Default.Phone,
                    isError = formSubmitted && phoneNumber.isBlank(),
                    errorMessage = if (formSubmitted && phoneNumber.isBlank()) "Phone required" else null,
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { passwordFocus.requestFocus() },
                    modifier = Modifier.focusRequester(phoneFocus),
                    keyboardType = KeyboardType.Phone
                )

                // Password
                GlossyPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
                    },
                    label = "Password",
                    isPasswordVisible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                    isError = (formSubmitted && password.isBlank()) || passwordMismatch,
                    errorMessage = when {
                        formSubmitted && password.isBlank() -> "Password required"
                        passwordMismatch -> "Passwords do not match"
                        else -> null
                    },
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { confirmFocus.requestFocus() },
                    modifier = Modifier.focusRequester(passwordFocus)
                )

                // Confirm Password
                GlossyPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordMismatch = password.isNotEmpty() && it != password
                    },
                    label = "Confirm Password",
                    isPasswordVisible = confirmPasswordVisible,
                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    isError = passwordMismatch,
                    errorMessage = if (passwordMismatch) "Passwords do not match" else null,
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { addressFocus.requestFocus() },
                    modifier = Modifier.focusRequester(confirmFocus)
                )

                // Address (optional)
                GlossyTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address (optional)",
                    leadingIcon = Icons.Default.Home,
                    colors = glossyColors,
                    imeAction = ImeAction.Next,
                    onNext = { pinFocus.requestFocus() },
                    modifier = Modifier.focusRequester(addressFocus)
                )

                // Pin Code (optional)
                GlossyTextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    label = "Pin Code (optional)",
                    leadingIcon = Icons.Default.LocationOn,
                    colors = glossyColors,
                    imeAction = ImeAction.Done,
                    onDone = { keyboardController?.hide() },
                    modifier = Modifier.focusRequester(pinFocus),
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sign Up Button (dark indigo, rounded)
                Button(
                    onClick = {
                        formSubmitted = true
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        if (name.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password != confirmPassword) {
                            passwordMismatch = true
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.createUser(
                            name = name,
                            password = password,
                            phoneNumber = phoneNumber,
                            email = email,
                            pinCode = pinCode,
                            address = address,
                            role = "user"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A2A6C),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "SIGN UP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // OR divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFD0D0E0)
                    )
                    Text(
                        text = " or ",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = Color(0xFF5A5A89),
                        fontSize = 12.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFD0D0E0)
                    )
                }

                // Social buttons (Google & Apple)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SocialButton(
                        icon = Icons.Default.Person,
                        text = "Google",
                        onClick = { Toast.makeText(context, "Google Sign In (demo)", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                    SocialButton(
                        icon = Icons.Default.PhoneIphone,
                        text = "Apple",
                        onClick = { Toast.makeText(context, "Apple Sign In (demo)", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Already have an account?
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color(0xFF5A5A89),
                        fontSize = 12.sp
                    )
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            navController.navigate(Routes.SignInRoutes)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2A6C),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ===== Reusable Glossy Components =====
@Composable
fun GlossyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    colors: TextFieldColors,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, modifier = Modifier.size(18.dp)) } },
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }
        },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        ),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun GlossyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    colors: TextFieldColors,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }
        },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onNext?.invoke() }),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun SocialButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(40.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.85f),
            contentColor = Color(0xFF1A2A6C)
        ),
        border = BorderStroke(1.dp, Color(0xFFD0D0E0))
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}