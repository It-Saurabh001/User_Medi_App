package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.ConfirmDialog
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.InfoTile
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.PersonAvatar
import com.saurabh.mediuserapp.ui.components.ShadeButton
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.dateTimeLabel
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Red600
import com.saurabh.mediuserapp.viewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.specificUserState.collectAsStateWithLifecycle()
    val user = state.success
    val snackbar = remember { SnackbarHostState() }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { if (state.success == null && !state.isLoading) viewModel.getSpecificUser() }

    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        handle?.get<String>(AUTH_MESSAGE_KEY)?.let {
            handle.remove<String>(AUTH_MESSAGE_KEY)
            snackbar.showSnackbar(it)
        }
    }
    val go: (Any) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }

    AppScreenScaffold(
        title = "My profile",
        snackbarHostState = snackbar,
        actions = {
            if (user != null) TileIconButton(Icons.Outlined.Edit, "Edit profile", { go(Routes.EditProfileRoutes) }, Modifier.padding(end = 8.dp))
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                user != null -> PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.getSpecificUser() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        ) {
                            item(key = "identity") {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    PersonAvatar(url = user.imageUrl, name = user.name, size = 96.dp)
                                    Text(
                                        user.name, style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (user.isApproved) ToneChip("Approved", Tone.Success) else ToneChip("Awaiting approval", Tone.Warning)
                                        if (user.isBlocked) ToneChip("Blocked", Tone.Danger)
                                    }
                                }
                            }
                            item(key = "details") {
                                DetailCard(title = "Account details", icon = Icons.Outlined.Person) {
                                    InfoTile("Email", user.email.ifBlank { "Not provided" }, icon = Icons.Outlined.Email)
                                    InfoTile("Phone", user.phoneNumber.orEmpty().ifBlank { "Not provided" }, icon = Icons.Outlined.Phone)
                                    InfoTile("Address", user.address.orEmpty().ifBlank { "Not provided" }, icon = Icons.Outlined.Home)
                                    InfoTile("Pin code", user.pinCode.orEmpty().ifBlank { "Not provided" }, icon = Icons.Outlined.PinDrop)
                                    InfoTile("Customer ID", user.userId, icon = Icons.Outlined.Fingerprint)
                                    InfoTile("Member since", user.createdAt.dateTimeLabel(), icon = Icons.Outlined.CalendarMonth)
                                }
                            }
                            item(key = "edit") {
                                ShadeButton("Edit profile", { go(Routes.EditProfileRoutes) }, Modifier.fillMaxWidth(), icon = Icons.Outlined.Edit)
                            }
                            item(key = "credentials") {
                                ShadeOutlinedButton(
                                    "Change email or password", { go(Routes.ChangeCredentialsRoutes) },
                                    Modifier.fillMaxWidth(), icon = Icons.Outlined.Lock
                                )
                            }
                            item(key = "settings") {
                                ShadeOutlinedButton(
                                    "Settings & about", { go(Routes.SettingsRoutes) },
                                    Modifier.fillMaxWidth(), icon = Icons.Outlined.Settings
                                )
                            }
                            item(key = "logout") {
                                ShadeOutlinedButton(
                                    text = "Log out", onClick = { confirmLogout = true },
                                    icon = Icons.AutoMirrored.Outlined.Logout, contentColor = Red600,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                state.error != null && !state.isLoading -> ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = { viewModel.getSpecificUser() },
                )
                else -> LoadingState()
            }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You will need to sign in again to place or track orders.",
            confirmText = "Log out",
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
                navController.navigate(Routes.SignInRoutes) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onDismiss = { confirmLogout = false },
        )
    }
}
