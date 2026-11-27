package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.ui.theme.ThemeMode

/** Circular profile photo in the top bar that opens the navigation drawer (48dp target, 0.97 scale). */
@Composable
fun DrawerAvatarButton(url: String?, name: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val source = remember { MutableInteractionSource() }
    val scale by source.pressScale()
    Box(
        modifier
            .size(48.dp)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Open menu" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
                .padding(2.dp)
        ) {
            PersonAvatar(url = url, name = name.ifBlank { "?" }, size = size)
        }
    }
}

/** Drawer: short profile header, shortcuts, theme and settings. */
@Composable
fun AppDrawerContent(
    user: UserDto?,
    themeMode: ThemeMode,
    onProfile: () -> Unit,
    onOrders: () -> Unit,
    onHistory: () -> Unit,
    onTheme: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerShape = MaterialTheme.shapes.large.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp)),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PersonAvatar(url = user?.imageUrl, name = user?.name.orEmpty().ifBlank { "?" }, size = 72.dp)
                Text(
                    user?.name?.ifBlank { null } ?: "Your account",
                    style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                user?.email?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                user?.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (user != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user.isApproved) ToneChip("Approved", Tone.Success) else ToneChip("Awaiting approval", Tone.Warning)
                    if (user.isBlocked) ToneChip("Blocked", Tone.Danger)
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NavRow("My profile", onProfile, subtitle = "View and edit your details", icon = Icons.Outlined.Person)
                NavRow("My orders", onOrders, icon = Icons.Outlined.ShoppingBag)
                NavRow("Purchase history", onHistory, icon = Icons.Outlined.History)
                NavRow("Theme", onTheme, subtitle = themeMode.label, icon = Icons.Outlined.DarkMode)
                NavRow("Settings", onSettings, subtitle = "App info and account", icon = Icons.Outlined.Settings)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        ShadeOutlinedButton(
            text = "Log out", onClick = onLogout,
            icon = Icons.AutoMirrored.Outlined.Logout, contentColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        )
    }
}

/** System / Light / Dark picker; selection applies immediately. */
@Composable
fun ThemeDialog(current: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Theme", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .selectable(
                                selected = mode == current, role = Role.RadioButton,
                                interactionSource = remember { MutableInteractionSource() }, indication = null,
                                onClick = { onSelect(mode) },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = mode == current, onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        confirmButton = { LinkButton("Done", onDismiss) },
    )
}
