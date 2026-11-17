package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.ui.theme.Shades

/** Cancel a pending order with an optional reason (U11: red confirm). */
@Composable
fun CancelOrderDialog(busy: Boolean, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var reason by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Cancel order?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("The pharmacy will not process this order. This cannot be undone.", style = MaterialTheme.typography.bodyMedium)
                GsTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    minLines = 2, maxLines = 4,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            LinkButton(
                text = if (busy) "Cancelling…" else "Cancel order",
                onClick = { onConfirm(reason.trim().ifBlank { null }) },
                enabled = !busy,
                shade = Shades.RedText
            )
        },
        dismissButton = { NeutralTextButton("Keep order", onDismiss, enabled = !busy) }
    )
}

/** Edit patient / doctor name of a pending order (PATCH orders/{id}). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderInfoSheet(
    patient0: String,
    doctor0: String,
    busy: Boolean,
    error: String?,
    patientError: String?,
    doctorError: String?,
    onSave: (patient: String?, doctor: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var patient by rememberSaveable { mutableStateOf(patient0) }
    var doctor by rememberSaveable { mutableStateOf(doctor0) }
    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit order details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            GsTextField(
                value = patient, onValueChange = { patient = it },
                label = { Text("Patient name", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }, singleLine = true, enabled = !busy,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                isError = patientError != null, supportingText = patientError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            GsTextField(
                value = doctor, onValueChange = { doctor = it },
                label = { Text("Doctor name", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }, singleLine = true, enabled = !busy,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                isError = doctorError != null, supportingText = doctorError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadeOutlinedButton("Cancel", onDismiss, Modifier.weight(1f), enabled = !busy)
                ShadeButton(
                    text = "Save",
                    onClick = {
                        onSave(
                            patient.trim().takeIf { it != patient0 },
                            doctor.trim().takeIf { it != doctor0 }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    isLoading = busy,
                    enabled = patient.trim() != patient0 || doctor.trim() != doctor0
                )
            }
        }
    }
}
