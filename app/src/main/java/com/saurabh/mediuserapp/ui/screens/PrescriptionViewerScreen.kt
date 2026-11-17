package com.saurabh.mediuserapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.theme.Sky100
import com.saurabh.mediuserapp.ui.theme.Sky700
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** Prescription of one order: zoomable 1:1-Fit image or PDF (open externally). Read-only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionViewerScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.orderDetailState.collectAsStateWithLifecycle()
    val order = state.success?.takeIf { it.orderId == orderId }
    val context = LocalContext.current
    LaunchedEffect(orderId) { if (order == null) viewModel.getOrderById(orderId) }

    val url = order?.prescriptionUrl?.takeIf { it.isNotBlank() }
    val isPdf = url?.substringBefore('?')?.endsWith(".pdf", ignoreCase = true) == true
    val openExternally: () -> Unit = {
        url?.let {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri()))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_LONG).show()
            }
        }
    }

    AppScreenScaffold(
        title = "Prescription",
        onBack = { navController.popBackStack() },
        actions = { if (url != null) TileIconButton(Icons.AutoMirrored.Outlined.OpenInNew, "Open in another app", openExternally, Modifier.padding(end = 8.dp)) },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                order == null && state.error != null -> ErrorState(
                    message = state.error.orEmpty(), onRetry = { viewModel.getOrderById(orderId) }
                )
                order == null -> LoadingState()
                url == null -> EmptyState(
                    title = "No prescription uploaded",
                    message = if (order.requiresPrescription) "Upload a prescription from the order details." else "This order does not need a prescription."
                )
                else -> Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RxHeader(order)
                    Box(
                        Modifier.fillMaxWidth().weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isPdf) PdfCard(openExternally) else ZoomableImage(url)
                    }
                    if (!isPdf) Text(
                        "Pinch or double-tap to zoom.", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun RxHeader(order: OrderDto) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (order.prescriptionVerified) ToneChip("Verified", Tone.Success) else ToneChip("Awaiting verification", Tone.Warning)
        if (order.doctorName.isNotBlank()) Text(
            "Dr. ${order.doctorName.removePrefix("Dr. ").removePrefix("Dr ")}", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ZoomableImage(url: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transform = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offset = if (scale == 1f) Offset.Zero else offset + pan
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = "Prescription image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f
                })
            }
            .transformable(transform)
            .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y },
        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp) } },
        error = {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Text("Image could not be loaded. Use the open button above.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        },
    )
}

@Composable
private fun PdfCard(onOpen: () -> Unit) {
    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = MaterialTheme.shapes.medium, color = Sky100, contentColor = Sky700, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(36.dp)) }
        }
        Text("PDF prescription", style = MaterialTheme.typography.titleMedium)
        Text("Open it in a PDF viewer to read it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        ShadeOutlinedButton("Open PDF", onOpen, icon = Icons.AutoMirrored.Outlined.OpenInNew)
    }
}
