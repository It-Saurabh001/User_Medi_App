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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.FormScaffold
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.PrescriptionSourceSheet
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.components.SelectedFileRow
import com.saurabh.mediuserapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediuserapp.ui.components.StatusChip
import com.saurabh.mediuserapp.ui.device.UploadResult
import com.saurabh.mediuserapp.ui.device.prepareUpload
import com.saurabh.mediuserapp.ui.device.rememberPermissionState
import com.saurabh.mediuserapp.ui.device.uploadMessage
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch
import java.io.File

/** POST orders/{id}/prescription (multipart) for a pending order. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPrescriptionScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val orderState by viewModel.orderDetailState.collectAsStateWithLifecycle()
    val state by viewModel.uploadPrescriptionState.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val order = orderState.success?.takeIf { it.orderId == orderId }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderId) {
        viewModel.resetUploadPrescriptionState()
        if (order == null) viewModel.getOrderById(orderId)
    }

    var doctor by rememberSaveable(order?.orderId) { mutableStateOf(order?.doctorName.orEmpty()) }
    var patient by rememberSaveable(order?.orderId) { mutableStateOf(order?.patientName.orEmpty()) }
    var fileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCamera by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var preparing by remember { mutableStateOf(false) }
    var fileError by remember { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    val busy = state.isLoading || preparing

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { fileUri = uri; fileError = null }
    }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { fileUri = uri; fileError = null }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { fileUri = pendingCamera; fileError = null }
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
        if (state.success != null) {
            viewModel.resetUploadPrescriptionState()
            navController.popBackStack()
        }
    }
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val api = state.appError as? AppError.Api
        fileError = api?.fields?.get("prescription") ?: state.appError.uploadMessage()
        formError = when {
            api?.code == ErrorCode.INVALID_TRANSITION -> "This order can no longer be changed."
            fileError != null -> null
            else -> error
        }
        if (api?.code == ErrorCode.INVALID_TRANSITION) viewModel.getOrderById(orderId)
        viewModel.resetUploadPrescriptionState()
    }

    fun submit() {
        if (busy) return
        val selected = fileUri
        if (selected == null) { fileError = "Choose a prescription file"; return }
        focusManager.clearFocus()
        formError = null
        scope.launch {
            preparing = true
            val uploads = (config as? UiState.Success)?.data?.uploads
            when (val r = prepareUpload(context, selected, uploads, imageOnly = false)) {
                is UploadResult.Ready -> {
                    preparing = false
                    viewModel.uploadPrescription(orderId, r.part, doctor.trim().ifBlank { null }, patient.trim().ifBlank { null })
                }
                is UploadResult.Rejected -> { preparing = false; fileError = r.message }
            }
        }
    }

    val editable = order?.status == OrderStatus.PENDING
    AppScreenScaffold(
        title = if (order?.prescriptionUrl.isNullOrBlank()) "Upload prescription" else "Replace prescription",
        onBack = { navController.popBackStack() },
        bottomBar = { if (order != null && editable) SaveBar("Upload", busy, ::submit, enabled = fileUri != null) }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                order != null -> FormScaffold {
                    DetailCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProductImage(order.imageUrl, null, size = 56.dp)
                            Column(Modifier.weight(1f)) {
                                Text(order.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(order.orderId, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            StatusChip(order.status)
                        }
                    }
                    if (!editable) {
                        AuthFormError("Prescriptions can only be changed while the order is pending.")
                    } else {
                        DetailCard(title = "Prescription file", icon = Icons.Outlined.Description) {
                            val selected = fileUri
                            if (selected == null) ShadeOutlinedButton(
                                text = "Choose file", onClick = { showSheet = true }, enabled = !busy,
                                icon = Icons.Outlined.UploadFile, modifier = Modifier.fillMaxWidth()
                            ) else SelectedFileRow(selected, enabled = !busy, onChange = { showSheet = true })
                            fileError?.let { SupportingText(it) }
                        }
                        GsTextField(
                            value = doctor, onValueChange = { doctor = it },
                            label = { Text("Doctor name", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth()
                        )
                        GsTextField(
                            value = patient, onValueChange = { patient = it },
                            label = { Text("Patient name (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                            singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth()
                        )
                        AuthFormError(formError)
                    }
                }
                orderState.error != null && !orderState.isLoading -> ErrorState(
                    message = orderState.error.orEmpty(), onRetry = { viewModel.getOrderById(orderId) }
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
            onRemove = if (fileUri != null) ({ fileUri = null }) else null
        )
    }
}
