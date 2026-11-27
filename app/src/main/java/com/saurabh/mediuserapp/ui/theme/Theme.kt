package com.saurabh.mediuserapp.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
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

private val DarkColorScheme = darkColorScheme(
    primary = Blue300, onPrimary = Blue900,
    primaryContainer = Blue800, onPrimaryContainer = Blue100,
    secondary = Sky200, onSecondary = Sky800,
    secondaryContainer = Night700, onSecondaryContainer = Blue100,
    tertiary = Green200, onTertiary = Green800,
    tertiaryContainer = Green800, onTertiaryContainer = Green100,
    error = Red200, onError = Red800,
    errorContainer = Red800, onErrorContainer = Red100,
    background = Night950, onBackground = NightInk,
    surface = Night900, onSurface = NightInk,
    surfaceVariant = Night800, onSurfaceVariant = NightMuted,
    outline = Night700, outlineVariant = Night700,
    surfaceTint = Blue300,
    surfaceContainerLowest = Night950,
    surfaceContainerLow = Night900,
    surfaceContainer = Night800,
    surfaceContainerHigh = Night800,
    surfaceContainerHighest = Night700,
    inverseSurface = NightInk, inverseOnSurface = Night900,
    inversePrimary = Blue600,
)

private val DarkStatusColors = StatusColors(
    successBg = Green800, successText = Green100,
    warningBg = Amber800, warningText = Amber100,
    dangerBg = Red800, dangerText = Red100,
    infoBg = Sky800, infoText = Sky100,
    neutralBg = Night700, neutralText = NightMuted,
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
    private val NightRow = Shade(Night900, Night800, Night700)
    @Composable fun rowShade(): Shade = if (MaterialTheme.isLight) Shades.BlueRow else NightRow
    @Composable fun secondaryButtonShade(): Shade = if (MaterialTheme.isLight) Shades.BlueSecondaryButton else NightRow
    @Composable fun iconButtonShade(): Shade = if (MaterialTheme.isLight) Shades.IconButton else Shade(Night800, Night700, Night600)
    @Composable fun chipSelectedShade(): Shade = if (MaterialTheme.isLight) Shades.BlueChipSelected else Shade(Blue800, Blue700, Blue700)
    @Composable fun familyShade(light: Shade): Shade = if (MaterialTheme.isLight) light else NightRow

    /** Unfocused Outline 1dp, focused Blue 500 2dp, error Red 600, label Muted -> Blue 600, White fill. */
    @Composable fun fieldColors(): TextFieldColors {
        val cs = MaterialTheme.colorScheme
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (MaterialTheme.isLight) Blue500 else Blue300,
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
fun MediUserAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // No dynamic colour; no ripple (press = same-family shade).
    CompositionLocalProvider(
        LocalStatusColors provides if (darkTheme) DarkStatusColors else StatusColors(),
        LocalRippleConfiguration provides null,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
