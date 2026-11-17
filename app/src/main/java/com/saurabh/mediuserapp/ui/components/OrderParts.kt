package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.material.icons.outlined.Description
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.ui.device.hasCamera
import com.saurabh.mediuserapp.ui.theme.Shades
import com.saurabh.mediuserapp.ui.theme.Transparent
import com.saurabh.mediuserapp.utils.capitalizeEachWord

/** - [value] + stepper; buttons disabled at [min]/[max]. 48dp targets. */
@Composable
fun QuantityStepper(value: Int, onValueChange: (Int) -> Unit, min: Int, max: Int, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TileIconButton(Icons.Outlined.Remove, "Decrease quantity", { onValueChange((value - 1).coerceAtLeast(min)) }, enabled = enabled && value > min)
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp)
        )
        TileIconButton(Icons.Outlined.Add, "Increase quantity", { onValueChange((value + 1).coerceAtMost(max)) }, enabled = enabled && value < max)
    }
}

/** Camera / Gallery / PDF / Remove picker for a prescription (images or PDF). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionSourceSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onPdf: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val camera = remember(context) { context.hasCamera() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                "Attach prescription", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() }
            )
            Text(
                "JPG, PNG, WEBP or PDF, up to the size allowed by the pharmacy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            if (camera) SheetRow("Take photo", Icons.Outlined.CameraAlt) { onDismiss(); onCamera() }
            SheetRow("Choose from gallery", Icons.Outlined.PhotoLibrary) { onDismiss(); onGallery() }
            SheetRow("Choose PDF", Icons.Outlined.PictureAsPdf) { onDismiss(); onPdf() }
            onRemove?.let { SheetRow("Remove", Icons.Outlined.Delete, danger = true) { onDismiss(); it() } }
        }
    }
}

@Composable
private fun SheetRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, danger: Boolean = false, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val bg by source.shadeColor(if (danger) Shades.RedCard.copy(normal = Transparent) else Shades.BlueRow.copy(normal = Transparent), "sheetRow")
    val fg = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = { Text(text, color = fg) },
        leadingContent = { Icon(icon, contentDescription = null, tint = fg) },
        colors = ListItemDefaults.colors(containerColor = Transparent),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).background(bg)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
    )
}

/** Order row for Orders / History: image, product, qty x price, date, total + status. */
@Composable
fun OrderCard(order: OrderDto, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    val base = modifier.fillMaxWidth()
    val m = if (onClick != null) base.shadeRow(onClick)
    else base.clip(shape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, shape)
    ListItem(
        leadingContent = { ProductImage(order.imageUrl, null, size = 56.dp) },
        headlineContent = {
            Text(order.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${order.quantity} × ${order.price.inr()}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(order.createdAt.dateTimeLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                if (order.requiresPrescription && order.prescriptionUrl.isNullOrBlank()) {
                    ToneChip(if (order.isSelfPrescribed()) "Self prescribed" else "No prescription", Tone.Info)
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(order.totalAmount.inr(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                StatusChip(order.status)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Transparent),
        modifier = m
    )
}

/** Picked prescription file: thumbnail (image) or document icon, display name, Change link. */
@Composable
fun SelectedFileRow(uri: Uri, enabled: Boolean, onChange: () -> Unit) {
    val context = LocalContext.current
    val (name, isPdf) = remember(uri) {
        val resolver = context.contentResolver
        val display = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull() ?: "Prescription"
        display to (resolver.getType(uri) == "application/pdf")
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ThumbImage(url = if (isPdf) null else uri.toString(), fallback = Icons.Outlined.Description, size = 48.dp)
        Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        LinkButton(text = "Change", onClick = onChange, enabled = enabled)
    }
}

/** Backend marks Rx orders placed without prescription details with doctor_name "Self prescribed". */
fun OrderDto.isSelfPrescribed(): Boolean =
    prescriptionUrl.isNullOrBlank() && doctorName.trim().equals("Self prescribed", ignoreCase = true)
