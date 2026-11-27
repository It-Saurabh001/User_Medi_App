package com.saurabh.mediuserapp.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediuserapp.ui.theme.isLight
import kotlin.math.roundToInt

/**
 * Full-screen photo preview that grows out of [origin] (the avatar's bounds in root coordinates).
 * [progress] 0 = at the avatar, 1 = fully open; the caller animates it and blurs the content behind.
 */
@Composable
fun AvatarPreviewOverlay(
    url: String?,
    name: String,
    origin: Rect,
    progress: Float,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    var rootOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootOffset = it.positionInRoot() }
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f * progress))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
    ) {
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        val targetPx = minOf(minOf(wPx, hPx) - with(density) { 48.dp.toPx() }, with(density) { 420.dp.toPx() })
        val startPx = origin.width.coerceAtLeast(1f)
        val sizePx = lerp(startPx, targetPx, progress)
        val startX = origin.left - rootOffset.x
        val startY = origin.top - rootOffset.y
        val x = lerp(startX, (wPx - targetPx) / 2f, progress)
        val y = lerp(startY, (hPx - targetPx) / 2f - with(density) { 24.dp.toPx() }, progress)
        val shape = RoundedCornerShape(lerp(50f, 6f, progress).roundToInt())
        val sizeDp = with(density) { sizePx.toDp() }

        Box(
            Modifier
                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                .size(sizeDp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            val fallback = @Composable {
                Text(
                    name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (url.isNullOrBlank()) fallback()
            else SubcomposeAsyncImage(
                model = url, contentDescription = "Profile photo of $name", contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(contentAlignment = Alignment.Center) { fallback() } },
                error = { Box(contentAlignment = Alignment.Center) { fallback() } },
            )
        }
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            color = if (MaterialTheme.isLight) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset { IntOffset(((wPx - targetPx) / 2f).roundToInt(), (y + sizePx + with(density) { 16.dp.toPx() }).roundToInt()) }
                .width(with(density) { targetPx.toDp() })
                .graphicsLayer { alpha = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f) },
        )
    }
}
