package com.saurabh.mediuserapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.dto.ProfileUpdateRequest
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.FormScaffold
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** PATCH users/me with a new email and/or password; `current_password` is required. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCredentialsScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.updateProfileState.collectAsStateWithLifecycle()
    val userState by viewModel.specificUserState.collectAsStateWithLifecycle()
    val currentEmail = userState.success?.email.orEmpty()
    val focusManager = LocalFocusManager.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var current by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailChanged by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetUpdateProfileState()
        if (userState.success == null) viewModel.getSpecificUser()
    }
    LaunchedEffect(state.success) {
        if (state.success != null) {
            viewModel.resetUpdateProfileState()
            val message = if (emailChanged) "Email updated. Use the new email next time you sign in." else "Password updated"
            runCatching { navController.previousBackStackEntry?.savedStateHandle?.set(AUTH_MESSAGE_KEY, message) }
            navController.popBackStack()
        }
    }
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val api = state.appError as? AppError.Api
        val fields = api?.fields.orEmpty()
        errors = buildMap {
            fields["email"]?.let { put("email", it) }
            fields["password"]?.let { put("password", it) }
            fields["current_password"]?.let { put("current", it) }
            if (api?.code == ErrorCode.UNAUTHORIZED && "current" !in this) put("current", "Current password is incorrect")
        }
        formError = if (errors.isEmpty()) error else null
        viewModel.resetUpdateProfileState()
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        errors = buildMap {
            if (e.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(e).matches()) put("email", "Enter a valid email")
            if (e.isNotEmpty() && e.equals(currentEmail, ignoreCase = true)) put("email", "This is already your email")
            if (password.isNotEmpty() && password.length !in 8..128) put("password", "8-128 characters")
            if (password.isNotEmpty() && confirm != password) put("confirm", "Passwords do not match")
            if (current.isEmpty()) put("current", "Enter your current password")
        }
        if (e.isEmpty() && password.isEmpty()) { formError = "Enter a new email or a new password"; return }
        if (errors.isNotEmpty()) return
        focusManager.clearFocus()
        formError = null
        emailChanged = e.isNotEmpty()
        viewModel.updateMe(
            ProfileUpdateRequest(
                email = e.ifEmpty { null },
                password = password.ifEmpty { null },
                currentPassword = current
            )
        )
    }

    AppScreenScaffold(title = "Email & password", onBack = { navController.popBackStack() }) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                FormScaffold {
                    DetailCard(title = "New email", icon = Icons.Outlined.Email) {
                        if (currentEmail.isNotBlank()) Text(
                            "Current: $currentEmail", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        GsTextField(
                            value = email, onValueChange = { email = it; errors = errors - "email"; formError = null },
                            label = { Text("New email", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                            singleLine = true, enabled = !state.isLoading,
                            isError = errors["email"] != null, supportingText = errors["email"]?.let { { SupportingText(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    DetailCard(title = "New password", icon = Icons.Outlined.Lock) {
                        PasswordField(
                            value = password, onValueChange = { password = it; errors = errors - "password"; formError = null },
                            label = "New password", error = errors["password"], hint = "8-128 characters",
                            imeAction = ImeAction.Next
                        )
                        PasswordField(
                            value = confirm, onValueChange = { confirm = it; errors = errors - "confirm" },
                            label = "Confirm password", error = errors["confirm"], imeAction = ImeAction.Next
                        )
                    }
                    DetailCard(title = "Confirm it's you", icon = Icons.Outlined.Key) {
                        PasswordField(
                            value = current, onValueChange = { current = it; errors = errors - "current" },
                            label = "Current password", error = errors["current"],
                            imeAction = ImeAction.Done, onDone = ::submit
                        )
                    }
                    AuthFormError(formError)
                }
            }
            SaveBar("Update", state.isLoading, ::submit)
        }
    }
}
