package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

private const val MIN_PASSWORD_LENGTH = 8
private const val MAX_PASSWORD_LENGTH = 128

/** Step 3: new password; sends email + otp + new_password. */
@Composable
fun ResetPasswordScreen(email: String, otp: String, viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.passwordResetOtpState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    fun backTo(route: Any, message: String) {
        runCatching {
            navController.getBackStackEntry(route).savedStateHandle[AUTH_MESSAGE_KEY] = message
            navController.popBackStack(route, inclusive = false)
        }.onFailure {
            navController.navigate(Routes.SignInRoutes) { popUpTo(0) { inclusive = true } }
        }
    }

    LaunchedEffect(state.success, state.error) {
        if (!submitted) return@LaunchedEffect
        if (state.success != null) {
            submitted = false
            viewModel.resetPasswordResetOtpState()
            backTo(Routes.SignInRoutes, "Password updated. Sign in with your new password.")
        } else if (state.error != null) {
            submitted = false
            val api = state.appError as? AppError.Api
            val otpError = state.appError.fieldError("otp")
            passwordError = state.appError.fieldError("new_password")
            viewModel.resetPasswordResetOtpState()
            when {
                api?.code == ErrorCode.RATE_LIMITED ->
                    backTo(Routes.ForgotPasswordRoutes, "Too many incorrect attempts. Request a new code.")
                otpError != null ->
                    backTo(Routes.ForgotPasswordOtpRoutes(email), "${state.error ?: "Invalid code"}: $otpError")
                passwordError == null -> formError = state.error
            }
        }
    }

    fun submit() {
        if (state.isLoading) return
        passwordError = when {
            password.length < MIN_PASSWORD_LENGTH -> "Minimum $MIN_PASSWORD_LENGTH characters"
            password.length > MAX_PASSWORD_LENGTH -> "Maximum $MAX_PASSWORD_LENGTH characters"
            else -> null
        }
        confirmError = if (confirm != password) "Passwords do not match" else null
        if (passwordError != null || confirmError != null) return
        focusManager.clearFocus()
        formError = null
        submitted = true
        viewModel.resetPasswordOtp(email = email, otp = otp, newPassword = password)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Reset password") { navController.navigateUp() } }
    ) { inner ->
        AuthFormColumn(modifier = Modifier.padding(inner), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.Password,
                title = "Choose a new password",
                subtitle = "Set a new password for $email."
            )
            Spacer(modifier = Modifier.height(32.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null; formError = null },
                label = "New password",
                error = passwordError,
                hint = "8-128 characters",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = confirm,
                onValueChange = { confirm = it; confirmError = null },
                label = "Confirm password",
                error = confirmError,
                onDone = ::submit
            )
            AuthFormError(formError)
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Reset password", isLoading = state.isLoading, onClick = ::submit)
            Spacer(modifier = Modifier.height(8.dp))
            LinkButton(
                text = "Change code",
                onClick = { navController.navigateUp() },
                enabled = !state.isLoading,
            )
        }
    }
}
