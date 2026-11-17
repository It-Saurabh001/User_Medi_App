package com.saurabh.mediuserapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.mediuserapp.viewModel.MyViewModel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Amber100
import com.saurabh.mediuserapp.ui.theme.Amber700
import com.saurabh.mediuserapp.ui.theme.Green100
import com.saurabh.mediuserapp.ui.theme.Green600
import com.saurabh.mediuserapp.ui.theme.Red100
import com.saurabh.mediuserapp.ui.theme.Red700
import com.saurabh.mediuserapp.ui.theme.Sky100
import com.saurabh.mediuserapp.ui.theme.Sky700

/** Centered status message: tone icon tile, title, message, one primary action, optional link. */
@Composable
internal fun StatusMessageScreen(
    icon: ImageVector,
    tone: Tone,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        AuthFormColumn(modifier = Modifier.padding(innerPadding)) {
            val (bg, fg) = when (tone) {
                Tone.Danger -> Red100 to Red700
                Tone.Warning -> Amber100 to Amber700
                Tone.Info -> Sky100 to Sky700
                Tone.Success -> Green100 to Green600
            }
            Surface(shape = MaterialTheme.shapes.extraLarge, color = bg, contentColor = fg, modifier = Modifier.size(88.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            AuthPrimaryButton(text = actionText, isLoading = false, onClick = onAction)
            if (secondaryText != null && onSecondary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinkButton(text = secondaryText, onClick = onSecondary)
            }
        }
    }
}

/** 403 ACCOUNT_NOT_APPROVED: "Check again" re-runs sign-in (SignIn keeps the typed email/password). */
@Composable
fun WaitingScreen(email: String, navController: NavHostController) {
    fun backToLogin(message: String?) {
        runCatching {
            if (message != null) navController.getBackStackEntry(Routes.SignInRoutes).savedStateHandle[AUTH_MESSAGE_KEY] = message
            navController.popBackStack(Routes.SignInRoutes, inclusive = false)
        }.onFailure {
            navController.navigate(Routes.SignInRoutes) { popUpTo(0) { inclusive = true } }
        }
    }
    BackHandler { backToLogin(null) }
    val who = email.ifBlank { "your account" }
    StatusMessageScreen(
        icon = Icons.Outlined.HourglassTop,
        tone = Tone.Warning,
        title = "Awaiting approval",
        message = "The pharmacy has not approved $who yet. You can sign in as soon as it is approved.",
        actionText = "Check again",
        onAction = { backToLogin("Tap Sign in to check your approval status.") },
        secondaryText = "Back to sign in",
        onSecondary = { backToLogin(null) }
    )
}

/** Blocking update screen (426 / min_app_version). Back is disabled. */
@Composable
fun UpgradeRequiredScreen(viewModel: MyViewModel) {
    val context = LocalContext.current
    val minVersion by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    BackHandler(enabled = true) { }
    val versionLine = minVersion?.takeIf { it != "latest" }?.let { " Minimum supported version: $it." } ?: ""
    StatusMessageScreen(
        icon = Icons.Outlined.SystemUpdate,
        tone = Tone.Info,
        title = "Update required",
        message = "This version of the app is no longer supported. Please update to continue.$versionLine",
        actionText = "Update",
        onAction = {
            val pkg = context.packageName
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()))
            } catch (_: ActivityNotFoundException) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$pkg".toUri()))
                }.onFailure { Toast.makeText(context, "No app store or browser found", Toast.LENGTH_LONG).show() }
            }
        }
    )
}

/** 403 ACCOUNT_BLOCKED. Back is disabled; "Log out" clears the session and returns to SignIn. */
@Composable
fun AccountBlockedScreen(viewModel: MyViewModel, navController: NavHostController) {
    BackHandler(enabled = true) { }
    StatusMessageScreen(
        icon = Icons.Outlined.Block,
        tone = Tone.Danger,
        title = "Account blocked",
        message = "Your account has been blocked. Please contact the pharmacy to restore access.",
        actionText = "Log out",
        onAction = {
            viewModel.acknowledgeAccountBlocked()
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    )
}
