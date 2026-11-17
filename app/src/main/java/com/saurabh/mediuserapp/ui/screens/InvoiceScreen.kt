package com.saurabh.mediuserapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.dto.InvoiceDto
import com.saurabh.mediuserapp.data.v1.dto.InvoiceItemDto
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.InfoTile
import com.saurabh.mediuserapp.ui.components.KeyValueRow
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.components.StatusChip
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.dateTimeLabel
import com.saurabh.mediuserapp.ui.components.showDate
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** GET orders/{id}/invoice (sold / returned orders only). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.invoiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(orderId) { viewModel.getInvoice(orderId) }

    val invoice = (state as? UiState.Success)?.data?.takeIf { it.order.orderId == orderId }
    val money = rememberInvoiceMoney(invoice?.currency)
    val share: () -> Unit = {
        invoice?.let { inv ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${inv.invoiceNumber}")
                putExtra(Intent.EXTRA_TEXT, inv.asShareText(money))
            }
            try {
                context.startActivity(Intent.createChooser(send, "Share invoice"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No app available to share the invoice", Toast.LENGTH_LONG).show()
            }
        }
    }

    AppScreenScaffold(
        title = invoice?.let { "Invoice ${it.invoiceNumber}" } ?: "Invoice",
        onBack = { navController.popBackStack() },
        actions = { if (invoice != null) TileIconButton(Icons.Outlined.Share, "Share invoice", share, Modifier.padding(end = 8.dp)) },
        bottomBar = { if (invoice != null) SaveBar("Share invoice", loading = false, onClick = share) },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            val s = state
            when {
                invoice != null -> PullToRefreshBox(
                    isRefreshing = s is UiState.Loading,
                    onRefresh = { viewModel.getInvoice(orderId) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) { InvoiceBody(invoice, money) }
                }
                s is UiState.Error -> ErrorState(
                    message = if ((s.error as? AppError.Api)?.code == ErrorCode.INVALID_TRANSITION)
                        "The invoice is available once the order is sold." else s.error.userMessage(),
                    onRetry = { viewModel.getInvoice(orderId) },
                )
                else -> LoadingState()
            }
        }
    }
}

@Composable
private fun InvoiceBody(inv: InvoiceDto, money: NumberFormat) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        item(key = "header") {
            DetailCard(title = "Invoice", icon = Icons.AutoMirrored.Outlined.ReceiptLong) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(inv.invoiceNumber, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "Order ${inv.order.orderId}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    StatusChip(inv.order.status)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTile("Issued", inv.issuedAt.dateTimeLabel(), Modifier.weight(1f))
                    InfoTile("Currency", inv.currency.ifBlank { "INR" }, Modifier.weight(1f))
                }
            }
        }

        item(key = "billTo") {
            val u = inv.user
            DetailCard(title = "Bill to", icon = Icons.Outlined.Person) {
                Text(u.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                listOfNotNull(
                    u.email.takeIf { it.isNotBlank() },
                    u.phoneNumber?.takeIf { it.isNotBlank() },
                    listOfNotNull(u.address?.takeIf { it.isNotBlank() }, u.pinCode?.takeIf { it.isNotBlank() })
                        .joinToString(" - ").takeIf { it.isNotBlank() },
                ).forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (inv.order.patientName.isNotBlank() || inv.order.doctorName.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (inv.order.patientName.isNotBlank()) InfoTile("Patient", inv.order.patientName, Modifier.weight(1f))
                        if (inv.order.doctorName.isNotBlank()) InfoTile("Doctor", inv.order.doctorName, Modifier.weight(1f))
                    }
                }
            }
        }

        item(key = "itemsHeader") {
            Text(
                "Items (${inv.items.size})", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp),
            )
        }
        itemsIndexed(inv.items, key = { i, it -> "${it.productId}_${it.batchId}_$i" }, contentType = { _, _ -> "line" }) { _, line ->
            InvoiceLine(line, money)
        }

        item(key = "totals") {
            DetailCard(title = "Totals") {
                KeyValueRow("Subtotal (taxable)", money.format(inv.subtotal))
                KeyValueRow("GST", money.format(inv.gstTotal))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Grand total", money.format(inv.grandTotal), emphasize = true)
                Text(
                    if (inv.pricesIncludeGst) "Prices include GST." else "GST added to prices.",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Generated ${inv.generatedAt.dateTimeLabel()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InvoiceLine(line: InvoiceItemDto, money: NumberFormat) {
    DetailCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(line.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    line.batchNo?.takeIf { it.isNotBlank() }?.let { ToneChip("Batch $it", Tone.Info) }
                    line.expiryDate?.let {
                        Text("Exp ${it.showDate()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                Text(
                    "${line.quantity} x ${money.format(line.unitPrice)} - GST ${line.gstPercent}% (${money.format(line.gstAmount)})",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                money.format(line.lineTotal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun rememberInvoiceMoney(currency: String?): NumberFormat = remember(currency) {
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 2
        runCatching { if (!currency.isNullOrBlank()) this.currency = Currency.getInstance(currency) }
    }
}

private fun InvoiceDto.asShareText(money: NumberFormat): String = buildString {
    appendLine("Invoice $invoiceNumber")
    appendLine("Issued: ${issuedAt.dateTimeLabel()}")
    appendLine("Order: ${order.orderId}")
    appendLine("Bill to: ${user.name}${if (user.phoneNumber.isNullOrBlank()) "" else " (${user.phoneNumber})"}")
    appendLine()
    items.forEach { l ->
        val batch = l.batchNo?.takeIf { it.isNotBlank() }?.let { " [$it]" }.orEmpty()
        appendLine("${l.productName}$batch  ${l.quantity} x ${money.format(l.unitPrice)} = ${money.format(l.lineTotal)}")
    }
    appendLine()
    appendLine("Subtotal: ${money.format(subtotal)}")
    appendLine("GST: ${money.format(gstTotal)}")
    append("Grand total: ${money.format(grandTotal)}")
}
