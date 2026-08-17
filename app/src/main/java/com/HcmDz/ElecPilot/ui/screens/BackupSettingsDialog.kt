package com.HcmDz.ElecPilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.BackupFileMode
import com.HcmDz.ElecPilot.data.BackupFormat
import com.HcmDz.ElecPilot.data.BackupModules
import com.HcmDz.ElecPilot.data.getBackupPreferences
import com.HcmDz.ElecPilot.data.saveBackupPreferences
import com.HcmDz.ElecPilot.util.BackupManager
import com.HcmDz.ElecPilot.util.BackupScheduler
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("NonObservableLocale")
@Composable
fun BackupSettingsDialog(
    onDismiss: () -> Unit,
    onBackupNow: () -> Unit,
    onBrowse: () -> Unit
) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(context.getBackupPreferences()) }
    var maxFilesValue by remember { mutableFloatStateOf(prefs.maxFiles.toFloat()) }
    var locationDisplay by remember { mutableStateOf(BackupManager.getDisplayPath(context, prefs)) }

    val treePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            prefs = prefs.copy(backupTreeUri = uri.toString())
            context.saveBackupPreferences(prefs)
            locationDisplay = BackupManager.getDisplayPath(context, prefs)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.backup_settings_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.backup_enable),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = prefs.enabled,
                        onCheckedChange = { enabled ->
                            prefs = prefs.copy(enabled = enabled)
                            context.saveBackupPreferences(prefs)
                            if (enabled) {
                                if (prefs.frequencyHours > 0) {
                                    BackupScheduler.schedulePeriodic(context, prefs.frequencyHours)
                                }
                            } else {
                                BackupScheduler.cancelPeriodic(context)
                            }
                        }
                    )
                }

                if (prefs.enabled) {
                    HorizontalDivider()

                    Text(
                        stringResource(R.string.backup_frequency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val frequencies = listOf(0, 1, 2, 3, 6, 12, 24, 72, 168, 360, 720)
                    var sliderIndex by remember {
                        mutableFloatStateOf(
                            frequencies.indexOf(prefs.frequencyHours).coerceAtLeast(0).toFloat()
                        )
                    }
                    Text(
                        formatFrequency(context, frequencies[sliderIndex.roundToInt()]),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (frequencies[sliderIndex.roundToInt()] == 0) {
                        Text(
                            stringResource(R.string.backup_frequency_on_change_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Slider(
                        value = sliderIndex,
                        onValueChange = { sliderIndex = it.roundToInt().toFloat() },
                        onValueChangeFinished = {
                            val hours = frequencies[sliderIndex.roundToInt()]
                            prefs = prefs.copy(frequencyHours = hours)
                            context.saveBackupPreferences(prefs)
                            if (hours > 0) {
                                BackupScheduler.schedulePeriodic(context, hours)
                            } else {
                                BackupScheduler.cancelPeriodic(context)
                            }
                        },
                        valueRange = 0f..(frequencies.size - 1).toFloat(),
                        steps = frequencies.size - 2
                    )

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.backup_location),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (locationDisplay.isNotBlank()) locationDisplay
                        else stringResource(R.string.backup_internal_storage),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    OutlinedButton(
                        onClick = { treePickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.backup_choose_folder))
                    }

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.backup_format),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.format == BackupFormat.CSV,
                            onClick = {
                                prefs = prefs.copy(format = BackupFormat.CSV)
                                context.saveBackupPreferences(prefs)
                            },
                            label = { Text("CSV") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = prefs.format == BackupFormat.EXCEL,
                            onClick = {
                                prefs = prefs.copy(format = BackupFormat.EXCEL)
                                context.saveBackupPreferences(prefs)
                            },
                            label = { Text("Excel") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.backup_modules),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val modules = listOf(BackupModules.DEPARTS, BackupModules.BOTH, BackupModules.PLC)
                    var moduleIndex by remember {
                        mutableFloatStateOf(
                            modules.indexOf(prefs.modules).coerceAtLeast(0).toFloat()
                        )
                    }
                    val moduleLabels = listOf(
                        R.string.backup_module_departs,
                        R.string.backup_module_both,
                        R.string.backup_module_plc
                    )
                    Text(
                        stringResource(moduleLabels[moduleIndex.roundToInt()]),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Slider(
                        value = moduleIndex,
                        onValueChange = { moduleIndex = it.roundToInt().toFloat() },
                        onValueChangeFinished = {
                            prefs = prefs.copy(modules = modules[moduleIndex.roundToInt()])
                            context.saveBackupPreferences(prefs)
                        },
                        valueRange = 0f..2f,
                        steps = 1
                    )

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.backup_file_mode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.fileMode == BackupFileMode.OVERWRITE,
                            onClick = {
                                prefs = prefs.copy(fileMode = BackupFileMode.OVERWRITE)
                                context.saveBackupPreferences(prefs)
                            },
                            label = { Text(stringResource(R.string.backup_mode_overwrite)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = prefs.fileMode == BackupFileMode.TIMESTAMPED,
                            onClick = {
                                prefs = prefs.copy(fileMode = BackupFileMode.TIMESTAMPED)
                                context.saveBackupPreferences(prefs)
                            },
                            label = { Text(stringResource(R.string.backup_mode_timestamp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (prefs.fileMode == BackupFileMode.TIMESTAMPED) {
                        Text(
                            stringResource(R.string.backup_max_files, maxFilesValue.toInt()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = maxFilesValue,
                            onValueChange = { maxFilesValue = it },
                            onValueChangeFinished = {
                                prefs = prefs.copy(maxFiles = maxFilesValue.toInt())
                                context.saveBackupPreferences(prefs)
                            },
                            valueRange = 1f..50f,
                            steps = 48
                        )
                    }

                    HorizontalDivider()

                    if (prefs.lastBackupTime > 0) {
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(prefs.lastBackupTime))
                        Text(
                            stringResource(R.string.backup_last_backup, dateStr),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val spaceUsed = BackupManager.getSpaceUsed(context)
                    Text(
                        stringResource(R.string.backup_space_used, BackupManager.formatSize(spaceUsed)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onBackupNow,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Backup,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.backup_manual))
                            }
                        }
                        OutlinedButton(
                            onClick = onBrowse,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FolderShared,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.backup_browse))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun formatFrequency(context: android.content.Context, hours: Int): String = when (hours) {
    0 -> context.getString(R.string.backup_frequency_on_change)
    else -> when {
        hours < 24 -> context.getString(R.string.backup_freq_hours, hours)
        hours == 24 -> context.getString(R.string.backup_freq_days, 1)
        else -> context.getString(R.string.backup_freq_days, hours / 24)
    }
}
