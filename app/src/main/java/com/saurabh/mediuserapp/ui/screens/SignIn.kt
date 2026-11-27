package com.saurabh.mediuserapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.R
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch

@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.loginUserState.collectAsStateWithLifecycle()
    val googleState by viewModel.googleLoginState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var googleEmail by rememberSaveable { mutableStateOf("") }
    val busy = state.isLoading || googleState.isLoading
    val focusManager = LocalFocusManager.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        handle?.get<String>(AUTH_MESSAGE_KEY)?.let {
            formError = it
            handle.remove<String>(AUTH_MESSAGE_KEY)
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val appError = state.appError
        when ((appError as? AppError.Api)?.code) {
            ErrorCode.ACCOUNT_NOT_APPROVED ->
                navController.navigate(Routes.WaitingRoutes(email.trim())) { launchSingleTop = true }
            ErrorCode.EMAIL_NOT_VERIFIED ->
                navController.navigate(Routes.EmailVerifyRoutes(email.trim(), sendCode = true)) {
                    launchSingleTop = true
                }
            ErrorCode.ACCOUNT_BLOCKED ->
                formError = "Your account has been blocked. Please contact the pharmacy."
            ErrorCode.RATE_LIMITED ->
                formError = "Too many attempts. Please wait a moment and try again."
            else -> {
                emailError = appError.fieldError("email")
                passwordError = appError.fieldError("password")
                formError = if (emailError == null && passwordError == null) error else null
            }
        }
        viewModel.resetLoginState()
    }

    LaunchedEffect(state.success) {
        val response = state.success ?: return@LaunchedEffect
        if (response.otpRequired) {
            navController.navigate(Routes.VerifyOtpRoutes(response.accountId)) { launchSingleTop = true }
        } else {
            formError = "Invalid email or password"
        }
        viewModel.resetLoginState()
    }

    LaunchedEffect(googleState.error) {
        val error = googleState.error ?: return@LaunchedEffect
        when ((googleState.appError as? AppError.Api)?.code) {
            ErrorCode.ACCOUNT_NOT_APPROVED ->
                navController.navigate(Routes.WaitingRoutes(googleEmail)) { launchSingleTop = true }
            ErrorCode.ACCOUNT_BLOCKED ->
                formError = "Your account has been blocked. Please contact the pharmacy."
            ErrorCode.RATE_LIMITED ->
                formError = "Too many attempts. Please wait a moment and try again."
            ErrorCode.GOOGLE_ACCOUNT_MISMATCH ->
                formError = "This account is linked to a different Google account. Use that one or your password."
            else -> formError = error
        }
        viewModel.resetGoogleLoginState()
    }

    LaunchedEffect(googleState.success) {
        googleState.success ?: return@LaunchedEffect
        viewModel.resetGoogleLoginState()
        navController.navigate(Routes.ProductRoutes) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun googleSignIn() {
        if (busy) return
        focusManager.clearFocus()
        formError = null
        scope.launch {
            when (val result = GoogleCredentials.requestIdToken(context)) {
                is GoogleSignInResult.Success -> {
                    googleEmail = result.email
                    viewModel.loginUserWithGoogle(result.idToken)
                }
                is GoogleSignInResult.Failure -> formError = result.message
                GoogleSignInResult.Cancelled -> Unit
            }
        }
    }

    fun submit() {
        if (busy) return
        val e = email.trim()
        emailError = when {
            e.isEmpty() -> "Enter your email"
            !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> "Enter a valid email"
            else -> null
        }
        passwordError = if (password.isEmpty()) "Enter your password" else null
        if (emailError != null || passwordError != null) return
        focusManager.clearFocus()
        formError = null
        viewModel.loginUser(e, password)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        AuthFormColumn(modifier = Modifier.padding(innerPadding)) {
            Image(
                painter = painterResource(R.drawable.glossy_blue_healthcare_shield_icon),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to order your medicines",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            GsTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null; formError = null },
                label = "Password",
                error = passwordError,
                imeAction = ImeAction.Done,
                onDone = ::submit
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                LinkButton(
                    text = "Forgot password?",
                    onClick = { navController.navigate(Routes.ForgotPasswordRoutes) { launchSingleTop = true } },
                    enabled = !state.isLoading,
                )
            }
            AuthFormError(formError)
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Sign in", isLoading = state.isLoading, onClick = ::submit)
            Spacer(modifier = Modifier.height(12.dp))
            GoogleSignInButton(enabled = !busy, onClick = ::googleSignIn)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            AuthFooterLink(
                prompt = "New here?",
                action = "Create account",
                onClick = {
                    if (!state.isLoading) navController.navigate(Routes.SignUpRoutes) { launchSingleTop = true }
                }
            )
        }
    }
}
