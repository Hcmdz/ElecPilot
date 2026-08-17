package com.HcmDz.ElecPilot.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.db.MotorEntity

@Composable
fun MotorFormDialog(
    initialMotor: MotorEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (MotorEntity) -> Unit
) {
    val isEdit = initialMotor != null && initialMotor.id != 0L
    var atelier by remember { mutableStateOf(initialMotor?.atelier ?: "") }
    var positionTGBT by remember { mutableStateOf(initialMotor?.positionTGBT ?: "") }
    var item by remember { mutableStateOf(initialMotor?.item ?: "") }
    var designation by remember { mutableStateOf(initialMotor?.designation ?: "") }
    var puissanceKW by remember { mutableStateOf(initialMotor?.puissanceKW ?: "") }
    var types by remember { mutableStateOf(initialMotor?.types ?: "") }
    var typesDeparts by remember { mutableStateOf(initialMotor?.typesDeparts ?: "") }
    var cable by remember { mutableStateOf(initialMotor?.cable ?: "") }
    var typeCable by remember { mutableStateOf(initialMotor?.typeCable ?: "") }
    var tgbt by remember { mutableStateOf(initialMotor?.tgbt ?: "") }

    val puissanceError = puissanceKW.isNotBlank() && puissanceKW.toFloatOrNull() == null
    val powerLabel = stringResource(R.string.field_power_kw)

    val fields = listOf(
        stringResource(R.string.field_atelier) to atelier,
        stringResource(R.string.field_position_tgbt) to positionTGBT,
        stringResource(R.string.field_item) to item,
        stringResource(R.string.field_designation) to designation,
        powerLabel to puissanceKW,
        stringResource(R.string.field_types) to types,
        stringResource(R.string.field_types_departs) to typesDeparts,
        stringResource(R.string.field_cable) to cable,
        stringResource(R.string.field_cable_type) to typeCable,
        stringResource(R.string.field_tgbt) to tgbt
    )
    val setters = listOf<(String) -> Unit>(
        { atelier = it },
        { positionTGBT = it },
        { item = it },
        { designation = it },
        { puissanceKW = it },
        { types = it },
        { typesDeparts = it },
        { cable = it },
        { typeCable = it },
        { tgbt = it }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) stringResource(R.string.form_title_edit_motor) else stringResource(R.string.form_title_add_motor)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                fields.forEachIndexed { index, (label, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = setters[index],
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = label == powerLabel && puissanceError,
                        supportingText = if (label == powerLabel && puissanceError) {
                            { Text(stringResource(R.string.form_field_numeric_required)) }
                        } else null
                    )
                    if (index < fields.lastIndex) Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val motor = if (isEdit) {
                        initialMotor.copy(
                            atelier = atelier, positionTGBT = positionTGBT,
                            item = item, designation = designation,
                            puissanceKW = puissanceKW, types = types,
                            typesDeparts = typesDeparts, cable = cable,
                            typeCable = typeCable, tgbt = tgbt
                        )
                    } else {
                        MotorEntity(
                            atelier = atelier, positionTGBT = positionTGBT,
                            item = item, designation = designation,
                            puissanceKW = puissanceKW, types = types,
                            typesDeparts = typesDeparts, cable = cable,
                            typeCable = typeCable, tgbt = tgbt
                        )
                    }
                    onConfirm(motor)
                },
                enabled = !puissanceError
            ) { Text(if (isEdit) stringResource(R.string.action_save) else stringResource(R.string.action_add)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
