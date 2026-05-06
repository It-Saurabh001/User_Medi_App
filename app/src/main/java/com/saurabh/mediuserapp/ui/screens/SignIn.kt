package com.saurabh.mediuserapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.LoadingOverlay
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "SignInScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_UI, "SignIn Screen ENTERED") }

    val context = LocalContext.current
    val state by viewModel.loginUserState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formSubmitted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }

    LoadingOverlay(isVisible = state.isLoading)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            AppLogger.e(AppLogger.TAG_UI, "Login ERROR: $err")
            snackbarHostState.showSnackbar(
                message = err,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
    }

    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (!navigated) {
                navigated = true
                AppLogger.i(AppLogger.TAG_UI, "Login SUCCESS, navigating to OTP")
                Toast.makeText(context, "OTP sent! Please verify.", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.VerifyOtpRoutes(userId = response.user_id)) {
                    popUpTo(Routes.SignInRoutes) { inclusive = true }
                }
            }
        }
    }

    // Animated gradient background (same as SignUp)
    val infiniteTransition = rememberInfiniteTransition(label = "bgGradient")
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBrush)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glassmorphic card (same styling as SignUp)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(32.dp),
                                spotColor = Color(0x33000000),
                                ambientColor = Color(0x22000000)
                            )
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.92f),
                                        Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            ),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // App Icon
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400)) +
                                        slideInVertically(initialOffsetY = { -it / 4 })
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .alpha(0.95f),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Welcome Back",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A2A6C),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Sign in to continue your journey",
                                fontSize = 16.sp,
                                color = Color(0xFF5A5A89),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Custom text field colors (matches SignUp)
                            val textFieldColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFB21F1F),
                                unfocusedBorderColor = Color(0xFFD0D0E0),
                                focusedTextColor = Color(0xFF1A2A6C),
                                unfocusedTextColor = Color(0xFF3A3A6C),
                                cursorColor = Color(0xFFB21F1F),
                                focusedLabelColor = Color(0xFFB21F1F),
                                unfocusedLabelColor = Color(0xFF5A5A89)
                            )

                            // Email Field
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                                        slideInVertically(initialOffsetY = { 40 })
                            ) {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    isError = formSubmitted && email.isBlank(),
                                    supportingText = {
                                        if (formSubmitted && email.isBlank()) {
                                            Text("Email is required", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { passwordFocusRequester.requestFocus() }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = textFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Password Field
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                        slideInVertically(initialOffsetY = { 40 })
                            ) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                null
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = formSubmitted && password.isBlank(),
                                    supportingText = {
                                        if (formSubmitted && password.isBlank()) {
                                            Text("Password is required", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { keyboardController?.hide() }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = textFieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(passwordFocusRequester)
                                )
                            }

                            // Forgot Password
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400, delayMillis = 300))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            keyboardController?.hide()
                                            navController.navigate(Routes.ForgotPasswordRoutes)
                                        }
                                    ) {
                                        Text(
                                            text = "Forgot Password?",
                                            color = Color(0xFFB21F1F),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Animated Sign In button (same gradient + scale)
                            var buttonScale by remember { mutableStateOf(1f) }
                            val scaleAnim by animateFloatAsState(
                                targetValue = buttonScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "buttonScale"
                            )

                            Button(
                                onClick = {
                                    buttonScale = 0.97f
                                    coroutineScope.launch {
                                        delay(100)
                                        buttonScale = 1f
                                    }
                                    formSubmitted = true
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    AppLogger.d(AppLogger.TAG_UI, "Sign In clicked").logAction("User Interaction")

                                    if (email.isBlank() || password.isBlank()) {
                                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    AppLogger.i(AppLogger.TAG_UI, "Login request for $email")
                                    viewModel.loginUser(email, password)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .scale(scaleAnim),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                enabled = !state.isLoading
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFB21F1F), Color(0xFFFD9A3E))
                                            ),
                                            shape = RoundedCornerShape(28.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "SIGN IN",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sign Up link
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = Color(0xFFE0E0E8),
                                        thickness = 1.dp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            text = "Don't have an account? ",
                                            color = Color(0xFF5A5A89)
                                        )
                                        TextButton(
                                            onClick = {
                                                keyboardController?.hide()
                                                navController.navigate(Routes.SignUpRoutes)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = "Sign Up",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB21F1F)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}