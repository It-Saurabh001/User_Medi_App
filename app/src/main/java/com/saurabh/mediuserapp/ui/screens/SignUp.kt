package com.saurabh.mediuserapp.ui.screens

import android.Manifest
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.saurabh.mediuserapp.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.ImageSourceSheet
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.PersonAvatar
import com.saurabh.mediuserapp.ui.device.UploadResult
import com.saurabh.mediuserapp.ui.device.prepareUpload
import com.saurabh.mediuserapp.ui.device.rememberPermissionState
import com.saurabh.mediuserapp.ui.device.uploadMessage
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch
import java.io.File

private val PHONE_REGEX = Regex("^\\+?[0-9]{7,15}$")
private val PIN_REGEX = Regex("^[0-9]{4,10}$")

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.createUserState.collectAsStateWithLifecycle()
    val config by viewModel.appConfigState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var pinCode by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
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
        val file = File(context.cacheDir, "signup_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        pendingCamera = uri
        camera.launch(uri)
    }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera() else formError = "Camera permission is needed to take a photo."
    }

    fun backToLogin(message: String? = null) {
        viewModel.resetCreateUserState()
        runCatching {
            if (message != null) navController.getBackStackEntry(Routes.SignInRoutes).savedStateHandle[AUTH_MESSAGE_KEY] = message
            navController.popBackStack(Routes.SignInRoutes, inclusive = false)
        }.onFailure {
            navController.navigate(Routes.SignInRoutes) { popUpTo(0) { inclusive = true } }
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val api = state.appError as? AppError.Api
        val fields = api?.fields.orEmpty()
        errors = buildMap {
            fields["name"]?.let { put("name", it) }
            fields["email"]?.let { put("email", it) }
            fields["phone_number"]?.let { put("phone", it) }
            fields["address"]?.let { put("address", it) }
            fields["pin_code"]?.let { put("pin", it) }
            fields["password"]?.let { put("password", it) }
            (fields["image"] ?: state.appError.uploadMessage())?.let { put("image", it) }
        }
        formError = if (errors.isEmpty()) error else null
        viewModel.resetCreateUserState()
    }

    fun submit() {
        if (busy) return
        val e = email.trim()
        val p = phone.trim()
        val pin = pinCode.trim()
        errors = buildMap {
            if (name.isBlank()) put("name", "Enter your name")
            when {
                e.isEmpty() -> put("email", "Enter your email")
                !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> put("email", "Enter a valid email")
            }
            if (p.isNotEmpty() && !PHONE_REGEX.matches(p)) put("phone", "7-15 digits, optional +")
            if (pin.isNotEmpty() && !PIN_REGEX.matches(pin)) put("pin", "4-10 digits")
            if (password.length !in 8..128) put("password", "8-128 characters")
            if (confirm != password) put("confirm", "Passwords do not match")
        }
        if (errors.isNotEmpty()) return
        focusManager.clearFocus()
        formError = null
        val selected = photo
        scope.launch {
            var image: com.saurabh.mediuserapp.repo.ImagePart? = null
            if (selected != null) {
                preparing = true
                val uploads = (config as? UiState.Success)?.data?.uploads
                when (val r = prepareUpload(context, selected, uploads, imageOnly = true)) {
                    is UploadResult.Ready -> image = r.part
                    is UploadResult.Rejected -> {
                        preparing = false
                        errors = mapOf("image" to r.message)
                        return@launch
                    }
                }
                preparing = false
            }
            viewModel.createUser(
                name = name.trim(),
                password = password,
                phoneNumber = p,
                email = e,
                pinCode = pin,
                address = address.trim(),
                image = image
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Create account") { if (!busy) backToLogin() } }
    ) { inner ->
        val created = state.success
        if (created != null) {
            AuthFormColumn(modifier = Modifier.padding(inner)) {
                AuthHeader(
                    icon = Icons.Outlined.HourglassTop,
                    title = "Awaiting approval",
                    subtitle = "Your account was created. The pharmacy must approve ${created.email} before you can sign in."
                )
                Spacer(Modifier.height(24.dp))
                AuthPrimaryButton(
                    text = "Back to sign in",
                    isLoading = false,
                    onClick = { backToLogin("Account created. Awaiting approval by the pharmacy.") }
                )
            }
            return@Scaffold
        }

        AuthFormColumn(modifier = Modifier.padding(inner), verticalArrangement = Arrangement.Top) {
            Image(
                painter = painterResource(R.drawable.glossy_blue_healthcare_shield_icon),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(12.dp))
            AuthHeader(
                icon = Icons.Outlined.PersonAdd,
                title = "New account",
                subtitle = "Create your account. The pharmacy approves it before your first sign-in."
            )
            Spacer(Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PersonAvatar(
                    url = photo?.toString(),
                    name = name.ifBlank { "?" },
                    size = 88.dp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !busy
                    ) { showSheet = true }
                )
                LinkButton(
                    text = if (photo == null) "Add photo (optional)" else "Change photo",
                    onClick = { showSheet = true },
                    enabled = !busy
                )
                errors["image"]?.let { SupportingText(it) }
            }
            Spacer(Modifier.height(8.dp))
            SignUpField(name, { name = it; errors = errors - "name" }, "Full name", Icons.Outlined.Person, errors["name"], KeyboardType.Text, !busy)
            SignUpField(email, { email = it; errors = errors - "email" }, "Email", Icons.Outlined.Email, errors["email"], KeyboardType.Email, !busy)
            SignUpField(phone, { phone = it; errors = errors - "phone" }, "Phone (optional)", Icons.Outlined.Phone, errors["phone"], KeyboardType.Phone, !busy)
            SignUpField(address, { address = it; errors = errors - "address" }, "Address (optional)", Icons.Outlined.Home, errors["address"], KeyboardType.Text, !busy)
            SignUpField(pinCode, { pinCode = it; errors = errors - "pin" }, "Pin code (optional)", Icons.Outlined.PinDrop, errors["pin"], KeyboardType.Number, !busy)
            PasswordField(
                value = password,
                onValueChange = { password = it; errors = errors - "password" },
                label = "Password",
                error = errors["password"],
                hint = "8-128 characters",
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(8.dp))
            PasswordField(
                value = confirm,
                onValueChange = { confirm = it; errors = errors - "confirm" },
                label = "Confirm password",
                error = errors["confirm"],
                imeAction = ImeAction.Done,
                onDone = ::submit
            )
            AuthFormError(formError)
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(text = "Create account", isLoading = busy, onClick = ::submit)
            AuthFooterLink(prompt = "Already have an account?", action = "Sign in", onClick = { if (!busy) backToLogin() })
        }
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
private fun SignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboardType: KeyboardType,
    enabled: Boolean,
    imeAction: ImeAction = ImeAction.Next,
) {
    GsTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { SupportingText(it) } },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
}
