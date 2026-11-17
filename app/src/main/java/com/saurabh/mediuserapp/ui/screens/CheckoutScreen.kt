package com.saurabh.mediuserapp.ui.screens

import android.Manifest
import android.net.Uri
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.repo.FilePart
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.FormScaffold
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.KeyValueRow
import com.saurabh.mediuserapp.ui.components.PrescriptionSourceSheet
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.components.SelectedFileRow
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.device.UploadResult
import com.saurabh.mediuserapp.ui.device.prepareUpload
import com.saurabh.mediuserapp.ui.device.rememberPermissionState
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.utils.screensState.CartItem
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch
import java.io.File

/** Multi-item checkout: summary, Rx details per prescription item, note, place order (POST checkouts). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(viewModel: MyViewModel, navController: NavController) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val state by viewModel.checkoutState.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var message by rememberSaveable { mutableStateOf("") }
    var sheetFor by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCamera by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var fileErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy = state.isLoading || preparing
    val total = cart.sumOf { it.price * it.quantity }

    fun setRx(productId: String, uri: String?) {
        viewModel.updateCart(productId) { it.copy(rxUri = uri) }
        fileErrors = fileErrors - productId
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val id = sheetFor
        if (uri != null && id != null) setRx(id, uri.toString())
    }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val id = sheetFor
        if (uri != null && id != null) setRx(id, uri.toString())
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val p = pendingCamera
        if (ok && p != null) setRx(p.first, p.second)
    }
    fun launchCamera(productId: String) {
        val file = File(context.cacheDir, "rx_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        pendingCamera = productId to uri.toString()
        camera.launch(uri)
    }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        val id = sheetFor
        if (granted && id != null) launchCamera(id)
    }

    LaunchedEffect(state.success) {
        val placed = state.success ?: return@LaunchedEffect
        navController.navigate(Routes.OrderPlacedRoutes(placed.orders.firstOrNull()?.orderId.orEmpty(), placed.checkoutId)) {
            popUpTo(Routes.CheckoutRoutes) { inclusive = true }
            launchSingleTop = true
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(if (state.itemErrors.isNotEmpty()) "Please fix the highlighted items." else it) }
    }

    fun submit() {
        if (busy || cart.isEmpty()) return
        focusManager.clearFocus()
        scope.launch {
            preparing = true
            val uploads = (config as? UiState.Success)?.data?.uploads
            val files = mutableMapOf<String, FilePart>()
            val errors = mutableMapOf<String, String>()
            cart.filter { it.needsPrescription && it.rxUri != null }.forEach { item ->
                when (val r = prepareUpload(context, item.rxUri!!.toUri(), uploads, imageOnly = false)) {
                    is UploadResult.Ready -> files[item.productId] = r.part
                    is UploadResult.Rejected -> errors[item.productId] = r.message
                }
            }
            preparing = false
            fileErrors = errors
            if (errors.isEmpty()) viewModel.checkout(message, files)
        }
    }

    AppScreenScaffold(
        title = "Checkout",
        onBack = { navController.popBackStack() },
        snackbarHostState = snackbar,
        bottomBar = { if (cart.isNotEmpty()) SaveBar("Place order · ${total.inr()}", busy, ::submit) }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            if (cart.isEmpty()) {
                EmptyState(title = "Your cart is empty", message = "Add medicines to your cart first.")
                return@Box
            }
            FormScaffold {
                cart.forEachIndexed { index, item ->
                    CheckoutLine(
                        item = item,
                        error = state.itemErrors[index] ?: fileErrors[item.productId],
                        enabled = !busy,
                        onDoctor = { v -> viewModel.updateCart(item.productId) { it.copy(doctorName = v) } },
                        onPatient = { v -> viewModel.updateCart(item.productId) { it.copy(patientName = v) } },
                        onAttach = { sheetFor = item.productId },
                    )
                }
                GsTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Note for the pharmacy (optional)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) },
                    minLines = 2, maxLines = 4, enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                )
                DetailCard(title = "Summary") {
                    KeyValueRow("Items", "${cart.size} (${cart.sumOf { it.quantity }} units)")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    KeyValueRow("Total", total.inr(), emphasize = true)
                    Text(
                        "Each item becomes its own order (same checkout). The pharmacy approves them separately.",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    val id = sheetFor
    if (id != null) {
        PrescriptionSourceSheet(
            onDismiss = { sheetFor = null },
            onCamera = { if (cameraPermission.isGranted) launchCamera(id) else cameraPermission.request() },
            onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onPdf = { pdf.launch(arrayOf("application/pdf")) },
            onRemove = if (cart.firstOrNull { it.productId == id }?.rxUri != null) ({ setRx(id, null) }) else null
        )
    }
}

@Composable
private fun CheckoutLine(
    item: CartItem,
    error: String?,
    enabled: Boolean,
    onDoctor: (String) -> Unit,
    onPatient: (String) -> Unit,
    onAttach: () -> Unit,
) {
    DetailCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(item.imageUrl, item.dosageForm, size = 56.dp)
            Column(Modifier.weight(1f)) {
                Text(item.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.quantity} × ${item.price.inr()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text((item.price * item.quantity).inr(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
        if (item.needsPrescription) {
            ToneChip("Rx Required", Tone.Info)
            Text(
                "Optional. Without a doctor name or prescription this item is marked \"Self prescribed\".",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            GsTextField(
                value = item.doctorName, onValueChange = onDoctor,
                label = { Text("Doctor name (optional)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()
            )
            GsTextField(
                value = item.patientName, onValueChange = onPatient,
                label = { Text("Patient name (optional)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()
            )
            val uri = item.rxUri
            if (uri == null) ShadeOutlinedButton(
                text = "Attach prescription (optional)", onClick = onAttach, enabled = enabled,
                icon = Icons.Outlined.UploadFile, modifier = Modifier.fillMaxWidth()
            ) else SelectedFileRow(Uri.parse(uri), enabled = enabled, onChange = onAttach)
        }
        if (error != null) Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}
