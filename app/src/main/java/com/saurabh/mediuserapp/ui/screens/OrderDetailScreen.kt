package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.CancelOrderDialog
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.EditOrderInfoSheet
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.InfoTile
import com.saurabh.mediuserapp.ui.components.KeyValueRow
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.NavRow
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.ShadeButton
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.StatusChip
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.dateTimeLabel
import com.saurabh.mediuserapp.ui.components.hasBilled
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.components.statusSummary
import com.saurabh.mediuserapp.ui.components.isSelfPrescribed
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Blue100
import com.saurabh.mediuserapp.ui.theme.Blue600
import com.saurabh.mediuserapp.ui.theme.FieldFill
import com.saurabh.mediuserapp.ui.theme.Muted
import com.saurabh.mediuserapp.ui.theme.Outline
import com.saurabh.mediuserapp.ui.theme.Red600
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.orderDetailState.collectAsStateWithLifecycle()
    val cancelState by viewModel.cancelOrderState.collectAsStateWithLifecycle()
    val editState by viewModel.editOrderState.collectAsStateWithLifecycle()
    val order = state.success?.takeIf { it.orderId == orderId }
    val snackbar = remember { SnackbarHostState() }
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }

    val checkoutGroups by viewModel.checkoutGroups.collectAsStateWithLifecycle()
    LaunchedEffect(order?.checkoutId, order?.status) { order?.checkoutId?.let(viewModel::loadCheckoutGroup) }

    LaunchedEffect(orderId) {
        viewModel.resetCancelOrderState()
        viewModel.resetEditOrderState()
        viewModel.getOrderById(orderId)
    }

    LaunchedEffect(cancelState.success, cancelState.error) {
        if (cancelState.success != null) {
            confirmCancel = false
            snackbar.showSnackbar("Order cancelled")
            viewModel.resetCancelOrderState()
        } else if (cancelState.error != null) {
            confirmCancel = false
            val transition = (cancelState.appError as? AppError.Api)?.code == ErrorCode.INVALID_TRANSITION
            if (transition) viewModel.getOrderById(orderId)
            snackbar.showSnackbar(if (transition) "Order status changed. It can no longer be cancelled." else cancelState.error.orEmpty())
            viewModel.resetCancelOrderState()
        }
    }

    LaunchedEffect(editState.success, editState.appError) {
        if (editState.success != null) {
            editing = false
            snackbar.showSnackbar("Order details updated")
            viewModel.resetEditOrderState()
        } else if ((editState.appError as? AppError.Api)?.code == ErrorCode.INVALID_TRANSITION) {
            editing = false
            viewModel.getOrderById(orderId)
            snackbar.showSnackbar("Order status changed. Details can no longer be edited.")
            viewModel.resetEditOrderState()
        }
    }

    AppScreenScaffold(
        title = "Order details",
        onBack = { navController.popBackStack() },
        snackbarHostState = snackbar,
        actions = {
            if (order?.status == OrderStatus.PENDING) {
                TileIconButton(Icons.Outlined.Edit, "Edit order details", { editing = true }, Modifier.padding(end = 8.dp))
            }
        },
        bottomBar = { if (order != null) OrderActionBar(order, cancelState.isLoading, navController, onCancel = { confirmCancel = true }) }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                order != null -> PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.getOrderById(orderId) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                        OrderBody(
                            order = order,
                            checkoutOrders = order.checkoutId?.let { checkoutGroups[it] },
                            onOpenCheckoutInvoice = {
                                order.checkoutId?.let { navController.navigate(Routes.CheckoutInvoiceRoutes(it)) { launchSingleTop = true } }
                            },
                            onOpenProduct = { navController.navigate(Routes.SpecificProductRoutes(order.productId)) { launchSingleTop = true } },
                            onOpenRx = { navController.navigate(Routes.PrescriptionViewerRoutes(order.orderId)) { launchSingleTop = true } }
                        )
                    }
                }
                state.error != null && !state.isLoading -> ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = { viewModel.getOrderById(orderId) }
                )
                else -> LoadingState()
            }
        }
    }

    if (confirmCancel && order != null) {
        CancelOrderDialog(
            busy = cancelState.isLoading,
            onConfirm = { reason -> viewModel.cancelOrder(order.orderId, reason) },
            onDismiss = { confirmCancel = false }
        )
    }
    if (editing && order != null) {
        val fields = (editState.appError as? AppError.Api)?.fields
        EditOrderInfoSheet(
            patient0 = order.patientName,
            doctor0 = order.doctorName,
            busy = editState.isLoading,
            error = editState.error?.takeIf { fields.isNullOrEmpty() },
            patientError = fields?.get("patient_name"),
            doctorError = fields?.get("doctor_name"),
            onSave = { p, d -> viewModel.updateOrderInfo(order.orderId, p, d) },
            onDismiss = { editing = false; viewModel.resetEditOrderState() }
        )
    }
}

@Composable
private fun OrderBody(
    order: OrderDto,
    onOpenProduct: () -> Unit,
    onOpenRx: () -> Unit,
    checkoutOrders: List<OrderDto>? = null,
    onOpenCheckoutInvoice: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        item(key = "header") {
            DetailCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Order", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.orderId, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    StatusChip(order.status)
                }
                when (order.status) {
                    OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SOLD -> ProgressTrack(order.status)
                    else -> Unit
                }
                if (order.status == OrderStatus.APPROVED) ToneChip("Being prepared", Tone.Info)
                if (order.status == OrderStatus.CANCELLED) {
                    InfoTile("Cancel reason", order.cancelReason.ifBlank { "No reason given" })
                }
            }
        }

        if (order.checkoutId != null) item(key = "checkout") {
            DetailCard(title = "Checkout", icon = Icons.Outlined.ShoppingCart) {
                val group = checkoutOrders.orEmpty()
                NavRow(
                    title = if (group.isEmpty()) "Part of a checkout" else "Part of checkout, ${group.size} items",
                    subtitle = when {
                        group.isEmpty() -> "Loading…"
                        group.hasBilled() -> "${group.statusSummary()} · view combined invoice"
                        else -> group.statusSummary()
                    },
                    onClick = { if (group.hasBilled()) onOpenCheckoutInvoice() },
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong
                )
                if (group.hasBilled()) ShadeOutlinedButton(
                    "View combined invoice", onOpenCheckoutInvoice, Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong
                )
            }
        }

        item(key = "product") {
            DetailCard(title = "Product", icon = Icons.Outlined.Receipt) {
                NavRowProduct(order, onOpenProduct)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Unit price", order.price.inr())
                KeyValueRow("Quantity", "${order.quantity}")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Total", order.totalAmount.inr(), emphasize = true)
            }
        }

        if (order.requiresPrescription || !order.prescriptionUrl.isNullOrBlank()) item(key = "rx") {
            DetailCard(title = "Prescription", icon = Icons.Outlined.Description) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.requiresPrescription) ToneChip("Rx Required", Tone.Info)
                    when {
                        order.isSelfPrescribed() -> ToneChip("Self prescribed", Tone.Info)
                        order.prescriptionUrl.isNullOrBlank() -> ToneChip("Not uploaded", Tone.Warning)
                        order.prescriptionVerified -> ToneChip("Verified", Tone.Success)
                        else -> ToneChip("Awaiting verification", Tone.Warning)
                    }
                }
                if (!order.prescriptionUrl.isNullOrBlank()) {
                    NavRow("View prescription", onOpenRx, subtitle = "Zoom or open the file", icon = Icons.Outlined.Description)
                } else {
                    InfoTile(
                        "Prescription",
                        if (order.isSelfPrescribed()) "Self prescribed. You can still upload a prescription while the order is pending."
                        else "No prescription uploaded yet"
                    )
                }
            }
        }

        item(key = "info") {
            DetailCard(title = "Order info", icon = Icons.Outlined.Person) {
                InfoTile("Ordered on", order.createdAt.dateTimeLabel(), icon = Icons.Outlined.CalendarMonth)
                if (order.patientName.isNotBlank()) InfoTile("Patient", order.patientName)
                if (order.doctorName.isNotBlank()) InfoTile("Doctor", order.doctorName)
                if (order.message.isNotBlank()) InfoTile("Your note", order.message)
                InfoTile("Last updated", order.updatedAt.dateTimeLabel())
            }
        }
    }
}

@Composable
private fun NavRowProduct(order: OrderDto, onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ProductImage(order.imageUrl, null, size = 64.dp)
        Column(Modifier.weight(1f)) {
            NavRow(order.productName.capitalizeEachWord(), onClick, subtitle = order.category.capitalizeEachWord().ifBlank { null })
        }
    }
}

@Composable
private fun ProgressTrack(status: OrderStatus) {
    val approved = status == OrderStatus.APPROVED || status == OrderStatus.SOLD
    val sold = status == OrderStatus.SOLD
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TrackStep("Placed", Icons.Outlined.Check, done = true)
        TrackLine(active = approved, modifier = Modifier.weight(1f))
        TrackStep("Approved", Icons.Outlined.Check, done = approved)
        TrackLine(active = sold, modifier = Modifier.weight(1f))
        TrackStep("Sold", Icons.Outlined.Sell, done = sold)
    }
}

@Composable
private fun TrackStep(label: String, icon: ImageVector, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(if (done) Blue100 else FieldFill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (done) Blue600 else Muted, modifier = Modifier.size(16.dp))
        }
        Text(
            label, style = MaterialTheme.typography.labelMedium, maxLines = 1,
            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrackLine(active: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.padding(horizontal = 4.dp).padding(bottom = 18.dp).height(2.dp).background(if (active) Blue600 else Outline))
}

/** One primary action per status (flow doc §5). */
@Composable
private fun OrderActionBar(order: OrderDto, busy: Boolean, navController: NavController, onCancel: () -> Unit) {
    val go: (Any) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }
    val content: (@Composable () -> Unit)? = when (order.status) {
        OrderStatus.PENDING -> {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShadeOutlinedButton("Cancel order", onCancel, Modifier.weight(1f), enabled = !busy, icon = Icons.Outlined.Close, contentColor = Red600)
                    if (order.requiresPrescription) ShadeButton(
                        text = if (order.prescriptionUrl.isNullOrBlank()) "Upload Rx" else "Replace Rx",
                        onClick = { go(Routes.UploadPrescriptionRoutes(order.orderId)) },
                        modifier = Modifier.weight(1f), icon = Icons.Outlined.UploadFile
                    )
                }
            }
        }
        OrderStatus.SOLD, OrderStatus.RETURNED -> {
            {
                ShadeButton(
                    "View invoice", { go(Routes.InvoiceRoutes(order.orderId)) },
                    Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Outlined.ReceiptLong
                )
            }
        }
        OrderStatus.CANCELLED -> {
            {
                ShadeButton(
                    "Order again", { go(Routes.PlaceOrderRoutes(order.productId)) },
                    Modifier.fillMaxWidth(), icon = Icons.Outlined.Replay
                )
            }
        }
        else -> null
    }
    if (content != null) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) { content() }
    }
}
