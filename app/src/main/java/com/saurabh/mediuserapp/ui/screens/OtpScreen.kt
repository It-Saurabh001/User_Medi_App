package com.saurabh.mediuserapp.ui.screens

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.shadeColor
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Blue500
import com.saurabh.mediuserapp.ui.theme.Shades
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEFAULT_OTP_LENGTH = 6
private const val DEFAULT_OTP_TTL_S = 300
private const val DEFAULT_OTP_MAX_ATTEMPTS = 5
private const val CUSTOMER_REQUIRED = "Customer account required"

@Composable
fun OtpScreen(accountId: String, viewModel: MyViewModel, navController: NavHostController) {
    val configState by viewModel.appConfigState.collectAsStateWithLifecycle()
    val otpConfig = (configState as? UiState.Success)?.data?.otp
    val otpLength = otpConfig?.length?.takeIf { it in 4..8 } ?: DEFAULT_OTP_LENGTH
    val ttlSeconds = otpConfig?.expiresIn?.takeIf { it > 0 } ?: DEFAULT_OTP_TTL_S
    val maxAttempts = otpConfig?.maxAttempts?.takeIf { it > 0 } ?: DEFAULT_OTP_MAX_ATTEMPTS

    val state by viewModel.verifyOtpState.collectAsStateWithLifecycle()
    val busy = state.isLoading
    val haptics = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val pasteScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var otp by rememberSaveable { mutableStateOf("") }
    var otpError by rememberSaveable { mutableStateOf<String?>(null) }
    var failedAttempts by rememberSaveable { mutableIntStateOf(0) }
    val deadline by rememberSaveable { mutableLongStateOf(SystemClock.elapsedRealtime() + ttlSeconds * 1000L) }
    var secondsLeft by remember { mutableIntStateOf(ttlSeconds) }
    var verified by rememberSaveable { mutableStateOf(false) }
    val expired = secondsLeft <= 0

    fun backToLogin(message: String) {
        runCatching {
            navController.getBackStackEntry(Routes.SignInRoutes).savedStateHandle[AUTH_MESSAGE_KEY] = message
            navController.popBackStack(Routes.SignInRoutes, inclusive = false)
        }.onFailure {
            navController.navigate(Routes.SignInRoutes) { popUpTo(0) { inclusive = true } }
        }
    }

    fun verify() {
        if (busy) return
        when {
            accountId.isBlank() -> backToLogin("Session expired. Sign in again.")
            expired -> otpError = "Code expired. Request a new one."
            otp.length != otpLength -> {
                otpError = "Enter all $otpLength digits"
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> {
                focusManager.clearFocus()
                viewModel.verifyUserOtp(accountId, otp)
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(deadline) {
        while (true) {
            secondsLeft = ((deadline - SystemClock.elapsedRealtime()) / 1000L).toInt().coerceAtLeast(0)
            if (secondsLeft == 0) break
            delay(1000L)
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val appError = state.appError
        val code = (appError as? AppError.Api)?.code
        viewModel.resetVerifyOtpState()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        otp = ""
        when {
            appError == null && error == CUSTOMER_REQUIRED -> backToLogin(error)
            code == ErrorCode.ACCOUNT_NOT_APPROVED ->
                navController.navigate(Routes.WaitingRoutes()) {
                    popUpTo(Routes.SignInRoutes) { inclusive = false }
                    launchSingleTop = true
                }
            code == ErrorCode.ACCOUNT_BLOCKED ->
                backToLogin("Your account has been blocked. Please contact the pharmacy.")
            code == ErrorCode.RATE_LIMITED -> backToLogin("Too many attempts. Sign in again.")
            else -> {
                failedAttempts++
                if (failedAttempts >= maxAttempts) backToLogin("Too many attempts. Sign in again.")
                else otpError = appError.fieldError("otp") ?: error
            }
        }
    }

    LaunchedEffect(state.success) {
        val response = state.success ?: return@LaunchedEffect
        verified = true
        viewModel.resetVerifyOtpState()
        if (response.accessToken.isNotBlank()) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(600L)
            navController.navigate(Routes.ProductRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            verified = false
            otpError = "Invalid code, please try again"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Verify code") { navController.navigateUp() } }
    ) { innerPadding ->
        if (verified) {
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Verified", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Taking you to your medicines",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        AuthFormColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.MarkEmailRead,
                title = "Check your email",
                subtitle = "Enter the $otpLength-digit code we sent to your registered email."
            )
            Spacer(modifier = Modifier.height(32.dp))
            OtpBoxes(
                value = otp,
                length = otpLength,
                isError = otpError != null,
                enabled = !busy && !expired,
                focusRequester = focusRequester,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit).take(otpLength)
                    if (digits != otp) {
                        otp = digits
                        otpError = null
                        if (digits.length == otpLength) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            verify()
                        }
                    }
                },
                onDone = ::verify
            )
            Spacer(modifier = Modifier.height(4.dp))
            SupportingText(otpError, if (expired) "Code expired" else "${otp.length}/$otpLength digits")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expired) "Code expired" else "Expires in %d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val pasteSource = remember { MutableInteractionSource() }
                val pasteColor by pasteSource.shadeColor(Shades.BlueText, "paste")
                TextButton(
                    interactionSource = pasteSource,
                    colors = ButtonDefaults.textButtonColors(contentColor = pasteColor),
                    onClick = {
                        pasteScope.launch {
                            clipboard.getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }
                                ?.getItemAt(0)?.text?.toString()
                                ?.filter(Char::isDigit)?.take(otpLength)
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { pasted ->
                                    otp = pasted
                                    otpError = null
                                    if (pasted.length == otpLength) verify()
                                }
                        }
                    },
                    enabled = !busy && !expired,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Paste", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Verify", isLoading = busy, onClick = ::verify)
            Spacer(modifier = Modifier.height(8.dp))
            LinkButton(
                text = "Didn't get a code? Send a new one",
                onClick = { backToLogin("Sign in again to get a new code.") },
                enabled = !busy,
            )
        }
    }
}

@Composable
internal fun OtpBoxes(
    value: String,
    length: Int,
    isError: Boolean,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = "Verification code, $length digits" },
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(length) { i ->
                    val char = value.getOrNull(i)
                    val focused = enabled && i == value.length.coerceAtMost(length - 1)
                    val borderColor by animateColorAsState(
                        when {
                            isError -> MaterialTheme.colorScheme.error
                            focused -> Blue500
                            else -> MaterialTheme.colorScheme.outline
                        },
                        tween(120), label = "otpBorder",
                    )
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 56.dp)
                            .aspectRatio(0.85f)
                            .border(if (focused || isError) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.medium)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char?.toString() ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    )
}
