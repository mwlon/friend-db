package com.example.frienddb.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnavailabilityDialog(
    onConfirm: (name: String, start: LocalDate, end: LocalDate, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name       by remember { mutableStateOf("") }
    var startDate  by remember { mutableStateOf(LocalDate.now()) }
    var endDate    by remember { mutableStateOf(LocalDate.now()) }
    var reason     by remember { mutableStateOf("") }

    var showPicker by remember { mutableStateOf(false) }

    val fmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Add Unavailability") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Friend name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick  = { showPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Range: ${startDate.format(fmt)} – ${endDate.format(fmt)}")
                }
                OutlinedTextField(
                    value         = reason,
                    onValueChange = { reason = it },
                    label         = { Text("Reason (optional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, startDate, endDate, reason) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showPicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis   = endDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedStartDateMillis?.let { start ->
                        state.selectedEndDateMillis?.let { end ->
                            startDate = Instant.ofEpochMilli(start).atZone(ZoneId.of("UTC")).toLocalDate()
                            endDate   = Instant.ofEpochMilli(end).atZone(ZoneId.of("UTC")).toLocalDate()
                            showPicker = false
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = state,
                title = { Text("Select date range", modifier = Modifier.fillMaxWidth().padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
