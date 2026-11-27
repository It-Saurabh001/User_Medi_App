package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.saurabh.mediuserapp.ui.components.ThemeDialog
import com.saurabh.mediuserapp.ui.theme.LocalThemeController
import com.saurabh.mediuserapp.ui.theme.ThemeMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.R
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.InfoTile
import com.saurabh.mediuserapp.ui.components.NavRow
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** Settings + About (admin Settings/About pattern): account shortcuts, appearance, version, about. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(viewModel: MyViewModel, navController: NavController) {
    val context = LocalContext.current
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.0"
    }
    val apiVersion = (config as? UiState.Success)?.data?.apiVersion
    val go: (Any) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }
    val themeController = LocalThemeController.current
    val themeMode = themeController?.mode?.collectAsStateWithLifecycle()?.value ?: ThemeMode.SYSTEM
    var showTheme by rememberSaveable { mutableStateOf(false) }
    if (showTheme && themeController != null) {
        ThemeDialog(themeMode, { themeController.set(it) }, { showTheme = false })
    }

    AppScreenScaffold(title = "Settings", onBack = { navController.popBackStack() }) { padding ->
        AdaptiveContent(Modifier.padding(padding), maxWidth = AdaptiveWidths.Form) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            ) {
                item(key = "account") {
                    DetailCard(title = "Account", icon = Icons.Outlined.Person) {
                        NavRow("Edit profile", { go(Routes.EditProfileRoutes) }, subtitle = "Name, phone, address, photo", icon = Icons.Outlined.Person)
                        NavRow("Email & password", { go(Routes.ChangeCredentialsRoutes) }, subtitle = "Needs your current password", icon = Icons.Outlined.Lock)
                    }
                }
                item(key = "app") {
                    DetailCard(title = "App", icon = Icons.Outlined.Info) {
                        NavRow("Theme", { showTheme = true }, subtitle = themeMode.label, icon = Icons.Outlined.Palette)
                        InfoTile("App version", version + (apiVersion?.let { " · API $it" } ?: ""), icon = Icons.Outlined.Tag)
                    }
                }
                item(key = "about") {
                    DetailCard(title = "About", icon = Icons.Outlined.Info) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Image(
                                painterResource(R.drawable.glossy_blue_healthcare_shield_icon), contentDescription = "App logo",
                                contentScale = ContentScale.Fit, modifier = Modifier.size(96.dp)
                            )
                            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                            ToneChip("Version $version", Tone.Info)
                        }
                        Text(
                            "Order medicines from your pharmacy: browse products, place orders with a prescription, " +
                                "track order status and view invoices for completed purchases.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
