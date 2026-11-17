package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.ImageZoomDialog
import com.saurabh.mediuserapp.ui.components.InfoTile
import com.saurabh.mediuserapp.ui.components.KeyValueRow
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.ProductStockChip
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.components.needsPrescription
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.specificProductState.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { viewModel.getSpecificProduct(productId) }
    val product = state.success?.takeIf { it.productId == productId }

    AppScreenScaffold(
        title = "Medicine",
        onBack = { navController.popBackStack() },
        bottomBar = {
            if (product != null) {
                val canOrder = product.inStock && product.sellableStock > 0
                SaveBar(
                    text = if (canOrder) "Buy · ${product.price.inr()}" else "Out of stock",
                    loading = false,
                    onClick = { navController.navigate(Routes.PlaceOrderRoutes(product.productId)) { launchSingleTop = true } },
                    enabled = canOrder
                )
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                product != null -> PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.getSpecificProduct(productId) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) { ProductDetailBody(product) }
                }
                state.error != null && !state.isLoading -> ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = { viewModel.getSpecificProduct(productId) }
                )
                else -> LoadingState()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailBody(p: ProductDto) {
    var zoom by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProductImage(
                url = p.imageUrl, dosageForm = p.dosageForm, size = 220.dp,
                modifier = if (!p.imageUrl.isNullOrBlank()) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) { zoom = true } else Modifier
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            val sub = listOf(p.genericName, p.strength).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotBlank()) Text(
                sub, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductStockChip(p)
            if (p.needsPrescription()) ToneChip("Rx Required", Tone.Info)
            if (p.scheduleType.isNotBlank() && p.scheduleType != "OTC") ToneChip("Schedule ${p.scheduleType}", Tone.Info)
        }

        DetailCard(title = "Price", icon = Icons.Outlined.Payments) {
            KeyValueRow("Price", p.price.inr(), emphasize = true)
            if (p.mrp > 0) KeyValueRow("MRP", p.mrp.inr())
            if (p.gstPercent > 0) KeyValueRow("GST (included)", "${p.gstPercent}%")
        }

        DetailCard(title = "Details", icon = Icons.Outlined.Info) {
            TileGrid(
                "Category" to p.category.capitalizeEachWord(),
                "Dosage form" to p.dosageForm,
                "Brand" to p.brandName,
                "Manufacturer" to p.manufacturer,
                "Strength" to p.strength,
                "Pack size" to p.packSize,
                "Unit" to p.unit,
                "Storage" to p.storageConditions,
            )
        }
        if (p.needsPrescription()) Text(
            "A valid prescription and the doctor's name are needed to order this medicine.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (zoom && !p.imageUrl.isNullOrBlank()) ImageZoomDialog(imageUrl = p.imageUrl, onDismiss = { zoom = false })
}

/** Two-column FieldFill info tiles; blank values are skipped. */
@Composable
private fun TileGrid(vararg items: Pair<String, String>) {
    val shown = items.filter { it.second.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shown.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value) -> InfoTile(label, value, Modifier.weight(1f)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}
