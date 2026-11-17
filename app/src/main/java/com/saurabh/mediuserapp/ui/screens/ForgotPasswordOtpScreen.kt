package com.saurabh.mediuserapp.ui.screens

import android.os.SystemClock
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay

private const val RESET_OTP_LENGTH = 6
private const val RESEND_COOLDOWN_S = 60

/** Step 2: collect the reset code only (verified by the server on confirm). */
@Composable
fun ForgotPasswordOtpScreen(email: String, viewModel: MyViewModel, navController: NavHostController) {
    val resendState by viewModel.passwordResetState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var otp by rememberSaveable { mutableStateOf("") }
    var otpError by rememberSaveable { mutableStateOf<String?>(null) }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var resending by rememberSaveable { mutableStateOf(false) }
    var resendAt by rememberSaveable { mutableLongStateOf(SystemClock.elapsedRealtime() + RESEND_COOLDOWN_S * 1000L) }
    var cooldown by remember { mutableIntStateOf(RESEND_COOLDOWN_S) }

    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        handle?.get<String>(AUTH_MESSAGE_KEY)?.let {
            otp = ""
            otpError = it
            handle.remove<String>(AUTH_MESSAGE_KEY)
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(resendAt) {
        while (true) {
            cooldown = ((resendAt - SystemClock.elapsedRealtime()) / 1000L).toInt().coerceAtLeast(0)
            if (cooldown == 0) break
            delay(1000L)
        }
    }

    LaunchedEffect(resendState.success, resendState.error) {
        if (!resending) return@LaunchedEffect
        if (resendState.success != null) {
            resending = false
            otp = ""
            otpError = null
            info = "A new code was sent to $email"
            resendAt = SystemClock.elapsedRealtime() + RESEND_COOLDOWN_S * 1000L
            viewModel.resetPasswordResetState()
        } else if (resendState.error != null) {
            resending = false
            otpError = if ((resendState.appError as? AppError.Api)?.code == ErrorCode.RATE_LIMITED)
                "Too many requests. Please wait a moment and try again." else resendState.error
            viewModel.resetPasswordResetState()
        }
    }

    fun next() {
        if (otp.length != RESET_OTP_LENGTH) {
            otpError = "Enter all $RESET_OTP_LENGTH digits"
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }
        focusManager.clearFocus()
        navController.navigate(Routes.ResetPasswordRoutes(email, otp)) { launchSingleTop = true }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Enter code") { navController.navigateUp() } }
    ) { inner ->
        AuthFormColumn(modifier = Modifier.padding(inner), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.MarkEmailRead,
                title = "Check your email",
                subtitle = info ?: "Enter the $RESET_OTP_LENGTH-digit reset code sent to $email."
            )
            Spacer(modifier = Modifier.height(32.dp))
            OtpBoxes(
                value = otp,
                length = RESET_OTP_LENGTH,
                isError = otpError != null,
                enabled = !resendState.isLoading,
                focusRequester = focusRequester,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit).take(RESET_OTP_LENGTH)
                    if (digits != otp) {
                        otp = digits
                        otpError = null
                    }
                },
                onDone = ::next
            )
            Spacer(modifier = Modifier.height(4.dp))
            SupportingText(otpError, "${otp.length}/$RESET_OTP_LENGTH digits")
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Continue", isLoading = false, onClick = ::next)
            Spacer(modifier = Modifier.height(8.dp))
            LinkButton(
                text = if (cooldown > 0) "Resend code in ${cooldown}s" else "Resend code",
                onClick = {
                    resending = true
                    viewModel.requestPasswordReset(email)
                },
                enabled = cooldown == 0 && !resendState.isLoading,
            )
        }
    }
}
