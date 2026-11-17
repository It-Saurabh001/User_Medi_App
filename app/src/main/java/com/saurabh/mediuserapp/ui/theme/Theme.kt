package com.saurabh.mediuserapp.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.saurabh.mediuserapp.data.v1.core.OrderStatus

private val LightColorScheme = lightColorScheme(
    primary = Blue600, onPrimary = White,
    primaryContainer = Blue100, onPrimaryContainer = Blue800,
    secondary = Sky600, onSecondary = White,
    secondaryContainer = Sky100, onSecondaryContainer = Sky800,
    tertiary = Green600, onTertiary = White,
    tertiaryContainer = Green100, onTertiaryContainer = Green800,
    error = Red600, onError = White,
    errorContainer = Red100, onErrorContainer = Red800,
    background = Blue50, onBackground = Ink,
    surface = White, onSurface = Ink,
    surfaceVariant = FieldFill, onSurfaceVariant = Muted,
    outline = Outline, outlineVariant = Outline,
    surfaceTint = Blue600,
    surfaceContainerLowest = White,
    surfaceContainerLow = Blue50,
    surfaceContainer = IconFill,
    surfaceContainerHigh = FieldFill,
    surfaceContainerHighest = Blue100,
    inverseSurface = Ink, inverseOnSurface = Blue50,
    inversePrimary = Blue300,
)

@Immutable
data class StatusColor(val container: Color, val content: Color)

/** Semantic pill colours (U14). Amber / Red / Sky text uses the 700 shade (bold). */
@Immutable
data class StatusColors(
    val successBg: Color = Green100, val successText: Color = Green600,
    val warningBg: Color = Amber100, val warningText: Color = Amber700,
    val dangerBg: Color = Red100, val dangerText: Color = Red700,
    val infoBg: Color = Sky100, val infoText: Color = Sky700,
    val neutralBg: Color = FieldFill, val neutralText: Color = Muted,
) {
    fun of(status: OrderStatus): StatusColor = when (status) {
        OrderStatus.PENDING -> StatusColor(warningBg, warningText)
        OrderStatus.APPROVED -> StatusColor(infoBg, infoText)
        OrderStatus.SOLD -> StatusColor(successBg, successText)
        OrderStatus.CANCELLED -> StatusColor(dangerBg, dangerText)
        OrderStatus.RETURNED, OrderStatus.UNKNOWN -> StatusColor(neutralBg, neutralText)
    }
}

val LocalStatusColors = staticCompositionLocalOf { StatusColors() }

val MaterialTheme.statusColors: StatusColors
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current

val MaterialTheme.isLight: Boolean
    @Composable @ReadOnlyComposable get() = colorScheme.background.luminance() > 0.5f

/** Theme-aware press shades and control colours (U5/U9/U13). */
object GsUi {
    @Composable fun rowShade(): Shade = Shades.BlueRow
    @Composable fun secondaryButtonShade(): Shade = Shades.BlueSecondaryButton
    @Composable fun iconButtonShade(): Shade = Shades.IconButton
    @Composable fun familyShade(light: Shade): Shade = light

    /** Unfocused Outline 1dp, focused Blue 500 2dp, error Red 600, label Muted -> Blue 600, White fill. */
    @Composable fun fieldColors(): TextFieldColors {
        val cs = MaterialTheme.colorScheme
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue500,
            unfocusedBorderColor = cs.outline,
            errorBorderColor = cs.error,
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
            focusedLeadingIconColor = cs.primary,
            unfocusedLeadingIconColor = cs.onSurfaceVariant,
            cursorColor = cs.primary,
            focusedContainerColor = cs.surface,
            unfocusedContainerColor = cs.surface,
            disabledContainerColor = cs.surface,
            errorContainerColor = cs.surface,
            focusedPlaceholderColor = cs.onSurfaceVariant,
            unfocusedPlaceholderColor = cs.onSurfaceVariant,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable fun topBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediUserAppTheme(content: @Composable () -> Unit) {
    // Light palette only, no dynamic colour; no ripple (press = same-family shade).
    CompositionLocalProvider(LocalStatusColors provides StatusColors(), LocalRippleConfiguration provides null) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
