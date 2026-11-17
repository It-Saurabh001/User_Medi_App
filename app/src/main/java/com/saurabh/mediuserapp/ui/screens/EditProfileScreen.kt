package com.saurabh.mediuserapp.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.dto.ProfileUpdateRequest
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.repo.ImagePart
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.FormScaffold
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.ImageSourceSheet
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.LoadingState
import com.saurabh.mediuserapp.ui.components.PersonAvatar
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.device.UploadResult
import com.saurabh.mediuserapp.ui.device.prepareUpload
import com.saurabh.mediuserapp.ui.device.rememberPermissionState
import com.saurabh.mediuserapp.ui.device.uploadMessage
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch
import java.io.File

private val PHONE_RE = Regex("^\\+?[0-9]{7,15}$")
private val PIN_RE = Regex("^[0-9]{4,10}$")

/** PATCH users/me: name, phone, address, pin code, photo (multipart `image`). Only changed fields are sent. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: MyViewModel, navController: NavController) {
    val userState by viewModel.specificUserState.collectAsStateWithLifecycle()
    val user = userState.success
    LaunchedEffect(Unit) {
        viewModel.resetUpdateProfileState()
        if (user == null) viewModel.getSpecificUser()
    }
    AppScreenScaffold(title = "Edit profile", onBack = { navController.popBackStack() }) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                user != null -> EditProfileForm(user, viewModel, navController)
                userState.error != null && !userState.isLoading -> ErrorState(userState.error.orEmpty(), { viewModel.getSpecificUser() })
                else -> LoadingState()
            }
        }
    }
}

@Composable
private fun EditProfileForm(user: UserDto, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.updateProfileState.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(user.name) }
    var phone by rememberSaveable { mutableStateOf(user.phoneNumber.orEmpty()) }
    var address by rememberSaveable { mutableStateOf(user.address.orEmpty()) }
    var pin by rememberSaveable { mutableStateOf(user.pinCode.orEmpty()) }
    var photo by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCamera by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var preparing by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    val busy = state.isLoading || preparing

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { photo = uri; errors = errors - "image" }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { photo = pendingCamera; errors = errors - "image" }
    }
    fun launchCamera() {
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        pendingCamera = uri
        camera.launch(uri)
    }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera() else formError = "Camera permission is needed to take a photo."
    }

    LaunchedEffect(state.success) {
        if (state.success != null) {
            viewModel.resetUpdateProfileState()
            runCatching { navController.previousBackStackEntry?.savedStateHandle?.set(AUTH_MESSAGE_KEY, "Profile updated") }
            navController.popBackStack()
        }
    }
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val fields = (state.appError as? AppError.Api)?.fields.orEmpty()
        errors = buildMap {
            fields["name"]?.let { put("name", it) }
            fields["phone_number"]?.let { put("phone", it) }
            fields["address"]?.let { put("address", it) }
            fields["pin_code"]?.let { put("pin", it) }
            (fields["image"] ?: state.appError.uploadMessage())?.let { put("image", it) }
        }
        formError = if (errors.isEmpty()) error else null
        viewModel.resetUpdateProfileState()
    }

    fun submit() {
        if (busy) return
        val n = name.trim(); val p = phone.trim(); val a = address.trim(); val c = pin.trim()
        errors = buildMap {
            if (n.isEmpty()) put("name", "Enter your name")
            if (p.isNotEmpty() && !PHONE_RE.matches(p)) put("phone", "7-15 digits, optional +")
            if (c.isNotEmpty() && !PIN_RE.matches(c)) put("pin", "4-10 digits")
        }
        if (errors.isNotEmpty()) return
        val request = ProfileUpdateRequest(
            name = n.takeIf { it != user.name },
            phoneNumber = p.takeIf { it.isNotEmpty() && it != user.phoneNumber.orEmpty() },
            address = a.takeIf { it.isNotEmpty() && it != user.address.orEmpty() },
            pinCode = c.takeIf { it.isNotEmpty() && it != user.pinCode.orEmpty() },
        )
        val selected = photo
        if (request == ProfileUpdateRequest() && selected == null) { formError = "Nothing to update"; return }
        focusManager.clearFocus()
        formError = null
        scope.launch {
            var image: ImagePart? = null
            if (selected != null) {
                preparing = true
                val uploads = (config as? UiState.Success)?.data?.uploads
                when (val r = prepareUpload(context, selected, uploads, imageOnly = true)) {
                    is UploadResult.Ready -> image = r.part
                    is UploadResult.Rejected -> { preparing = false; errors = mapOf("image" to r.message); return@launch }
                }
                preparing = false
            }
            viewModel.updateMe(request, image)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            FormScaffold {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    PersonAvatar(
                        url = photo?.toString() ?: user.imageUrl,
                        name = name.ifBlank { user.name },
                        size = 96.dp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = !busy
                        ) { showSheet = true }
                    )
                    LinkButton(text = "Change photo", onClick = { showSheet = true }, enabled = !busy)
                    errors["image"]?.let { SupportingText(it) }
                }
                ProfileField(name, { name = it; errors = errors - "name" }, "Full name", Icons.Outlined.Person, errors["name"], KeyboardType.Text, !busy, KeyboardCapitalization.Words)
                ProfileField(phone, { phone = it; errors = errors - "phone" }, "Phone", Icons.Outlined.Phone, errors["phone"], KeyboardType.Phone, !busy)
                ProfileField(address, { address = it; errors = errors - "address" }, "Address", Icons.Outlined.Home, errors["address"], KeyboardType.Text, !busy, KeyboardCapitalization.Sentences)
                ProfileField(pin, { pin = it; errors = errors - "pin" }, "Pin code", Icons.Outlined.PinDrop, errors["pin"], KeyboardType.Number, !busy, imeAction = ImeAction.Done)
                AuthFormError(formError)
            }
        }
        SaveBar("Save changes", busy, ::submit)
    }

    if (showSheet) {
        ImageSourceSheet(
            onDismiss = { showSheet = false },
            onCamera = { if (cameraPermission.isGranted) launchCamera() else cameraPermission.request() },
            onGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onRemove = if (photo != null) ({ photo = null }) else null,
            title = "Profile photo"
        )
    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboardType: KeyboardType,
    enabled: Boolean,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
) {
    GsTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization, imeAction = imeAction),
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { SupportingText(it) } },
        modifier = Modifier.fillMaxWidth()
    )
}
