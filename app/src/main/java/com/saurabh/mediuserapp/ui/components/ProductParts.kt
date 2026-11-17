package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.ui.theme.Amber100
import com.saurabh.mediuserapp.ui.theme.Amber700
import com.saurabh.mediuserapp.ui.theme.Blue100
import com.saurabh.mediuserapp.ui.theme.Blue600
import com.saurabh.mediuserapp.ui.theme.Outline
import com.saurabh.mediuserapp.ui.theme.Sky100
import com.saurabh.mediuserapp.ui.theme.Sky700
import com.saurabh.mediuserapp.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

/** 1:1 medicine image (ContentScale.Fit) with a dosage-form fallback icon (U15). */
@Composable
fun ProductImage(url: String?, dosageForm: String?, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val shape = MaterialTheme.shapes.small
    val form = dosageForm.orEmpty().lowercase()
    val (icon, bg, fg) = when {
        "syrup" in form || "drop" in form || "liquid" in form -> Triple(Icons.Outlined.WaterDrop, Blue100, Blue600)
        "injection" in form || "vial" in form -> Triple(Icons.Outlined.Vaccines, Amber100, Amber700)
        "ointment" in form || "cream" in form || "gel" in form -> Triple(Icons.Outlined.Healing, Sky100, Sky700)
        else -> Triple(Icons.Outlined.Medication, Sky100, Sky700)
    }
    val fallback = @Composable {
        Box(Modifier.size(size).clip(shape).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(size / 2))
        }
    }
    if (url.isNullOrBlank()) { Box(modifier) { fallback() }; return }
    SubcomposeAsyncImage(
        model = url, contentDescription = null, contentScale = ContentScale.Fit,
        modifier = modifier.size(size).clip(shape).background(White).border(1.dp, Outline, shape).padding(4.dp),
        loading = { fallback() }, error = { fallback() }
    )
}

/** Availability pill for customers: Out of stock / Only N left / In stock. */
@Composable
fun ProductStockChip(p: ProductDto, modifier: Modifier = Modifier) {
    val available = p.sellableStock
    when {
        !p.inStock || available <= 0 -> ToneChip("Out of stock", Tone.Danger, modifier)
        available <= p.reorderLevel -> ToneChip("Only $available left", Tone.Warning, modifier)
        else -> ToneChip("In stock", Tone.Success, modifier)
    }
}

private val INR: NumberFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())

fun Double.inr(): String = INR.format(this)

private val RX_SCHEDULES = setOf("H", "H1", "X", "NARCOTIC")

/** Same rule as the backend create-order check: requires_prescription or a prescription schedule. */
fun ProductDto.needsPrescription(): Boolean =
    requiresPrescription || scheduleType.trim().uppercase() in RX_SCHEDULES
