package com.saurabh.mediuserapp.ui.screens

import android.Manifest
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.repo.FilePart
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.FormScaffold
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.KeyValueRow
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.PrescriptionSourceSheet
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.ProductStockChip
import com.saurabh.mediuserapp.ui.components.QuantityStepper
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.components.SelectedFileRow
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.ThumbImage
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.components.needsPrescription
import com.saurabh.mediuserapp.ui.device.UploadResult
import com.saurabh.mediuserapp.ui.device.prepareUpload
import com.saurabh.mediuserapp.ui.device.rememberPermissionState
import com.saurabh.mediuserapp.ui.device.uploadMessage
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch
import java.io.File

private const val MAX_ORDER_QTY = 100

/** One order = one product. Rx section when the product requires a prescription. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOrderScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val productState by viewModel.specificProductState.collectAsStateWithLifecycle()
    val state by viewModel.createOrderState.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val product = productState.success?.takeIf { it.productId == productId }

    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!started) { started = true; viewModel.resetCreateOrderState() }
        if (product == null) viewModel.getSpecificProduct(productId)
    }

    var qty by rememberSaveable { mutableIntStateOf(1) }
    var message by rememberSaveable { mutableStateOf("") }
    var doctor by rememberSaveable { mutableStateOf("") }
    var patient by rememberSaveable { mutableStateOf("") }
    var rxUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCamera by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var lastSignature by rememberSaveable { mutableStateOf<String?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    val busy = state.isLoading || preparing

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { rxUri = uri; errors = errors - "rx" }
    }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { rxUri = uri; errors = errors - "rx" }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { rxUri = pendingCamera; errors = errors - "rx" }
    }
    fun launchCamera() {
        val file = File(context.cacheDir, "rx_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        pendingCamera = uri
        camera.launch(uri)
    }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera() else formError = "Camera permission is needed to take a photo."
    }

    LaunchedEffect(state.success) {
        val order = state.success ?: return@LaunchedEffect
        navController.navigate(Routes.OrderPlacedRoutes(order.orderId)) {
            popUpTo(Routes.PlaceOrderRoutes(productId)) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(state.error, state.appError) {
        val error = state.error ?: return@LaunchedEffect
        val api = state.appError as? AppError.Api
        val fields = api?.fields.orEmpty()
        errors = buildMap {
            (fields["quantity"] ?: if (api?.code == ErrorCode.OUT_OF_STOCK) error else null)?.let { put("qty", it) }
            fields["doctor_name"]?.let { put("doctor", it) }
            fields["patient_name"]?.let { put("patient", it) }
            fields["message"]?.let { put("message", it) }
            (fields["prescription"] ?: fields["prescription_url"] ?: state.appError.uploadMessage())?.let { put("rx", it) }
        }
        formError = when (api?.code) {
            ErrorCode.OUT_OF_STOCK -> "Not enough stock for this quantity."
            ErrorCode.PRESCRIPTION_REQUIRED -> "This medicine needs a prescription."
            else -> if (errors.isEmpty()) error else null
        }
    }

    fun submit(p: ProductDto) {
        if (busy) return
        val maxQty = p.sellableStock.coerceAtMost(MAX_ORDER_QTY)
        errors = buildMap {
            if (qty < 1 || qty > maxQty) put("qty", "Choose between 1 and $maxQty")
        }
        if (errors.isNotEmpty()) {
            formError = errors.values.first()
            scope.launch { snackbar.showSnackbar(formError.orEmpty()) }
            return
        }
        focusManager.clearFocus()
        formError = null
        val signature = listOf(qty, message.trim(), doctor.trim(), patient.trim(), rxUri).joinToString("|")
        if (lastSignature != null && lastSignature != signature) viewModel.resetCreateOrderState()
        lastSignature = signature
        val selected = rxUri.takeIf { p.needsPrescription() }
        scope.launch {
            var part: FilePart? = null
            if (selected != null) {
                preparing = true
                val uploads = (config as? UiState.Success)?.data?.uploads
                when (val r = prepareUpload(context, selected, uploads, imageOnly = false)) {
                    is UploadResult.Ready -> part = r.part
                    is UploadResult.Rejected -> {
                        preparing = false
                        errors = mapOf("rx" to r.message)
                        return@launch
                    }
                }
                preparing = false
            }
            viewModel.createOrder(
                productId = p.productId,
                quantity = qty.toString(),
                message = message.trim(),
                doctorName = doctor.trim().takeIf { p.needsPrescription() && it.isNotEmpty() },
                patientName = patient.trim().takeIf { p.needsPrescription() },
                prescription = part
            )
        }
    }

    AppScreenScaffold(
        title = "Place order",
        onBack = { navController.popBackStack() },
        snackbarHostState = snackbar,
        bottomBar = {
            if (product != null) {
                val canOrder = product.inStock && product.sellableStock > 0
                SaveBar(
                    text = if (canOrder) "Place order · ${(product.price * qty).inr()}" else "Out of stock",
                    loading = busy,
                    onClick = { submit(product) },
                    enabled = canOrder
                )
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                product != null -> FormScaffold {
                    val maxQty = product.sellableStock.coerceIn(1, MAX_ORDER_QTY)
                    DetailCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProductImage(product.imageUrl, product.dosageForm, size = 64.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(product.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val sub = listOf(product.strength, product.packSize).filter { it.isNotBlank() }.joinToString(" · ")
                                if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                ProductStockChip(product)
                            }
                        }
                    }

                    DetailCard(title = "Quantity", icon = Icons.Outlined.Inventory2) {
                        QuantityStepper(
                            value = qty,
                            onValueChange = { qty = it; errors = errors - "qty" },
                            min = 1, max = maxQty, enabled = !busy
                        )
                        errors["qty"]?.let { SupportingText(it) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        KeyValueRow("Unit price", product.price.inr())
                        KeyValueRow("Total", (product.price * qty).inr(), emphasize = true)
                    }

                    if (product.needsPrescription()) {
                        DetailCard(title = "Prescription", icon = Icons.Outlined.Description) {
                            ToneChip("Rx Required", Tone.Info)
                            Text(
                                "Prescription and doctor name are optional. Without them the order is marked " +
                                    "\"Self prescribed\" and the pharmacy reviews it before dispensing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GsTextField(
                                value = doctor,
                                onValueChange = { doctor = it; errors = errors - "doctor" },
                                label = { Text("Doctor name (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                singleLine = true,
                                enabled = !busy,
                                isError = errors["doctor"] != null,
                                supportingText = errors["doctor"]?.let { { SupportingText(it) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            GsTextField(
                                value = patient,
                                onValueChange = { patient = it; errors = errors - "patient" },
                                label = { Text("Patient name (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                singleLine = true,
                                enabled = !busy,
                                isError = errors["patient"] != null,
                                supportingText = errors["patient"]?.let { { SupportingText(it) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            val selected = rxUri
                            if (selected == null) {
                                ShadeOutlinedButton(
                                    text = "Attach prescription (optional)",
                                    onClick = { showSheet = true },
                                    enabled = !busy,
                                    icon = Icons.Outlined.UploadFile,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                SelectedFileRow(selected, enabled = !busy, onChange = { showSheet = true })
                            }
                            errors["rx"]?.let { SupportingText(it) }
                        }
                    }

                    GsTextField(
                        value = message,
                        onValueChange = { message = it; errors = errors - "message" },
                        label = { Text("Note for the pharmacy (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) },
                        minLines = 2,
                        maxLines = 4,
                        enabled = !busy,
                        isError = errors["message"] != null,
                        supportingText = errors["message"]?.let { { SupportingText(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AuthFormError(formError)
                }
                productState.error != null && !productState.isLoading -> ErrorState(
                    message = productState.error.orEmpty(),
                    onRetry = { viewModel.getSpecificProduct(productId) }
                )
                else -> LoadingState()
            }
        }
    }

    if (showSheet) {
        PrescriptionSourceSheet(
            onDismiss = { showSheet = false },
            onCamera = { if (cameraPermission.isGranted) launchCamera() else cameraPermission.request() },
            onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onPdf = { pdf.launch(arrayOf("application/pdf")) },
            onRemove = if (rxUri != null) ({ rxUri = null }) else null
        )
    }
}
