package com.saurabh.mediuserapp.ui.screens

import androidx.activity.compose.BackHandler
import com.saurabh.mediuserapp.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextAlign
import android.util.Patterns
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.data.v1.error.AppError
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

private val PHONE_REGEX = Regex("^\\+?[0-9]{7,15}$")

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.createUserState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    val busy = state.isLoading

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
            fields["password"]?.let { put("password", it) }
        }
        formError = if (errors.isEmpty()) error else null
        viewModel.resetCreateUserState()
    }

    // Server requires email verification: continue on the code screen (SignUp leaves the back stack).
    LaunchedEffect(state.success) {
        val user = state.success ?: return@LaunchedEffect
        if (user.otpRequired == true) {
            viewModel.resetCreateUserState()
            navController.navigate(Routes.EmailVerifyRoutes(user.email)) {
                popUpTo(Routes.SignInRoutes) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    fun submit() {
        if (busy) return
        val e = email.trim()
        val p = phone.trim()
        errors = buildMap {
            if (name.isBlank()) put("name", "Enter your name")
            when {
                e.isEmpty() -> put("email", "Enter your email")
                !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> put("email", "Enter a valid email")
            }
            if (p.isNotEmpty() && !PHONE_REGEX.matches(p)) put("phone", "7-15 digits, optional +")
            if (password.length !in 8..128) put("password", "8-128 characters")
            if (confirm != password) put("confirm", "Passwords do not match")
        }
        if (errors.isNotEmpty()) return
        focusManager.clearFocus()
        formError = null
        // Address, PIN code and photo are added later from the profile screen.
        viewModel.createUser(
            name = name.trim(),
            password = password,
            phoneNumber = p,
            email = e,
            pinCode = "",
            address = ""
        )
    }

    // No top bar: system back returns to sign in (and clears this screen's state).
    BackHandler { if (!busy) backToLogin() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { inner ->
        val created = state.success
        if (created != null && created.otpRequired != true) {
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

        AuthFormColumn(modifier = Modifier.padding(inner)) {
            Image(
                painter = painterResource(R.drawable.glossy_blue_healthcare_shield_icon),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Create account",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sign up to order your medicines",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            SignUpField(name, { name = it; errors = errors - "name" }, "Full name", Icons.Outlined.Person, errors["name"], KeyboardType.Text, !busy)
            SignUpField(email, { email = it; errors = errors - "email" }, "Email", Icons.Outlined.Email, errors["email"], KeyboardType.Email, !busy)
            SignUpField(phone, { phone = it; errors = errors - "phone" }, "Phone (optional)", Icons.Outlined.Phone, errors["phone"], KeyboardType.Phone, !busy)
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
