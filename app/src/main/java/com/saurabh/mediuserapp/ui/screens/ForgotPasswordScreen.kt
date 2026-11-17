package com.saurabh.mediuserapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** Step 1: request a reset code for the customer email. */
@Composable
fun ForgotPasswordScreen(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.passwordResetState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var email by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        handle?.get<String>(AUTH_MESSAGE_KEY)?.let {
            formError = it
            handle.remove<String>(AUTH_MESSAGE_KEY)
        }
    }

    LaunchedEffect(state.success, state.error) {
        if (!submitted) return@LaunchedEffect
        if (state.success != null) {
            submitted = false
            viewModel.resetPasswordResetState()
            navController.navigate(Routes.ForgotPasswordOtpRoutes(email.trim())) { launchSingleTop = true }
        } else if (state.error != null) {
            submitted = false
            val api = state.appError as? AppError.Api
            emailError = state.appError.fieldError("email")
            formError = when {
                emailError != null -> null
                api?.code == ErrorCode.RATE_LIMITED -> "Too many requests. Please wait a moment and try again."
                else -> state.error
            }
            viewModel.resetPasswordResetState()
        }
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        emailError = when {
            e.isEmpty() -> "Enter your email"
            !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> "Enter a valid email"
            else -> null
        }
        if (emailError != null) return
        focusManager.clearFocus()
        formError = null
        submitted = true
        viewModel.requestPasswordReset(e)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Forgot password") { navController.navigateUp() } }
    ) { inner ->
        AuthFormColumn(modifier = Modifier.padding(inner), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.LockReset,
                title = "Reset your password",
                subtitle = "Enter your registered email. If an account exists, we will send a 6-digit reset code."
            )
            Spacer(modifier = Modifier.height(32.dp))
            GsTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            AuthFormError(formError)
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Send code", isLoading = state.isLoading, onClick = ::submit)
        }
    }
}
