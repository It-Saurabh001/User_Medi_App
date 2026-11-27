package com.saurabh.mediuserapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

private const val CODE_LENGTH = 6
private const val RESEND_COOLDOWN_S = 60

/** Confirms the account's email with the code sent by signup, unverified sign-in or an email change. */
@Composable
fun EmailVerifyScreen(email: String, sendCode: Boolean, viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.emailVerifyState.collectAsStateWithLifecycle()
    val loggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val busy = state.isVerifying
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var otp by rememberSaveable { mutableStateOf("") }
    var otpError by rememberSaveable { mutableStateOf<String?>(null) }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var started by rememberSaveable { mutableStateOf(false) }
    var cooldown by rememberSaveable { mutableIntStateOf(if (sendCode) 0 else RESEND_COOLDOWN_S) }

    fun leave(message: String?) {
        viewModel.resetEmailVerifyState()
        if (loggedIn) {
            navController.popBackStack()
            return
        }
        runCatching {
            if (message != null) navController.getBackStackEntry(Routes.SignInRoutes).savedStateHandle[AUTH_MESSAGE_KEY] = message
            check(navController.popBackStack(Routes.SignInRoutes, inclusive = false))
        }.onFailure {
            navController.navigate(Routes.SignInRoutes) { popUpTo(0) { inclusive = true } }
        }
    }

    fun resend() {
        if (state.isResending || cooldown > 0) return
        otpError = null
        info = null
        cooldown = RESEND_COOLDOWN_S
        viewModel.resendVerificationOtp(email)
    }

    fun verify() {
        if (busy) return
        if (otp.length != CODE_LENGTH) {
            otpError = "Enter all $CODE_LENGTH digits"
            return
        }
        focusManager.clearFocus()
        viewModel.verifyEmail(email, otp)
    }

    BackHandler { leave(null) }

    LaunchedEffect(Unit) {
        if (!started) {
            started = true
            viewModel.resetEmailVerifyState()
            if (sendCode) resend()
        }
        focusRequester.requestFocus()
    }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000L)
            cooldown--
        }
    }

    LaunchedEffect(state) {
        when {
            state.verified -> leave("Email verified. Sign in to continue.")
            state.codeSent -> {
                info = "A new code has been sent."
                viewModel.resetEmailVerifyState()
            }
            state.error != null -> {
                val api = state.appError as? AppError.Api
                otp = ""
                otpError = when (api?.code) {
                    // 429: a code was sent moments ago (resend) or too many wrong tries (verify).
                    ErrorCode.RATE_LIMITED -> "Please wait, then use the latest code or request a new one."
                    else -> state.appError.fieldError("otp")?.let { "Code $it" } ?: state.error
                }
                viewModel.resetEmailVerifyState()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Verify email") { leave(null) } }
    ) { innerPadding ->
        AuthFormColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.MarkEmailRead,
                title = "Verify your email",
                subtitle = "Enter the $CODE_LENGTH-digit code sent to $email."
            )
            Spacer(Modifier.height(32.dp))
            OtpBoxes(
                value = otp,
                length = CODE_LENGTH,
                isError = otpError != null,
                enabled = !busy,
                focusRequester = focusRequester,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit).take(CODE_LENGTH)
                    if (digits != otp) {
                        otp = digits
                        otpError = null
                        if (digits.length == CODE_LENGTH) verify()
                    }
                },
                onDone = ::verify
            )
            Spacer(Modifier.height(4.dp))
            SupportingText(otpError, info ?: "${otp.length}/$CODE_LENGTH digits")
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(text = "Verify email", isLoading = busy, onClick = ::verify)
            Spacer(Modifier.height(8.dp))
            LinkButton(
                text = if (cooldown > 0) "Send a new code in ${cooldown}s" else "Send a new code",
                onClick = ::resend,
                enabled = !busy && !state.isResending && cooldown == 0,
            )
        }
    }
}
