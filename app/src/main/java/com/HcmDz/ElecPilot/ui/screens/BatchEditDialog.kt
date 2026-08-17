package com.HcmDz.ElecPilot.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEditDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (field: String, value: String) -> Unit
) {
    val fields = listOf(stringResource(R.string.field_atelier), stringResource(R.string.field_tgbt), stringResource(R.string.field_position_tgbt), stringResource(R.string.field_item), stringResource(R.string.field_designation),
        stringResource(R.string.field_power_kw), stringResource(R.string.field_types), stringResource(R.string.field_types_departs), stringResource(R.string.field_cable), stringResource(R.string.field_cable_type))
    var selectedField by remember { mutableStateOf(fields.first()) }
    var fieldExpanded by remember { mutableStateOf(false) }
    var newValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.dialog_batch_edit_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dialog_batch_edit_count, selectedCount),
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = fieldExpanded,
                    onExpandedChange = { fieldExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedField,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dialog_batch_edit_field_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = fieldExpanded,
                        onDismissRequest = { fieldExpanded = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            fields.forEach { field ->
                                DropdownMenuItem(
                                    text = { Text(field) },
                                    onClick = {
                                        selectedField = field
                                        fieldExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text(stringResource(R.string.dialog_batch_edit_new_value)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedField, newValue) },
                enabled = newValue.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
