package com.HcmDz.ElecPilot.ui.screens

import android.annotation.SuppressLint
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.BackupFormat
import com.HcmDz.ElecPilot.data.SyncStatus
import com.HcmDz.ElecPilot.data.getCloudBackupPreferences
import com.HcmDz.ElecPilot.data.saveCloudBackupPreferences
import com.HcmDz.ElecPilot.util.CloudBackupScheduler
import com.HcmDz.ElecPilot.util.RcloneAuthActivity
import com.HcmDz.ElecPilot.util.RcloneDriveService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NonObservableLocale")
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CloudBackupSettingsDialog(
    onDismiss: () -> Unit,
    onBackupNow: () -> Unit,
    onBrowse: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prefs by remember { mutableStateOf(context.getCloudBackupPreferences()) }
    var isSignedIn by remember { mutableStateOf(false) }
    var accountEmail by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            isSignedIn = RcloneDriveService.isSignedIn(context)
            accountEmail = RcloneDriveService.getAccountEmail(context)
        }
    }
    var showRemoteSetup by remember { mutableStateOf(false) }
    var selectedRemoteType by remember { mutableStateOf("drive") }
    var remoteName by remember { mutableStateOf("elecpilot") }
    var customRemoteName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isAuthInProgress by remember { mutableStateOf(false) }
    var authStatus by remember { mutableStateOf("") }
    var pendingAuthRemoteType by remember { mutableStateOf("") }
    var pendingAuthRemoteName by remember { mutableStateOf("") }

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_CANCELED && !isSignedIn) {
            isAuthInProgress = true
            authStatus = "Saving authentication..."
        }
    }

    LaunchedEffect(isAuthInProgress) {
        if (isAuthInProgress) {
            withContext(Dispatchers.IO) {
                withTimeoutOrNull(30_000L) {
                    RcloneAuthActivity.tokenFlow.collect { token ->
                        if (token != null) {
                            try {
                                val remoteType = pendingAuthRemoteType
                                val remoteName = pendingAuthRemoteName
                                RcloneDriveService.saveRemoteConfig(context, remoteType, remoteName, token)
                                RcloneDriveService.saveAccountEmail(context, remoteName)
                                withContext(Dispatchers.Main) {
                                    isSignedIn = true
                                    accountEmail = remoteName
                                    showRemoteSetup = false
                                    authStatus = ""
                                    isAuthInProgress = false
                                }
                                RcloneAuthActivity.clearToken()
                                return@collect
                            } catch (e: Exception) {
                                android.util.Log.e("CloudBackup", "Auth failed", e)
                                withContext(Dispatchers.Main) {
                                    authStatus = context.getString(R.string.auth_error_generic)
                                    isAuthInProgress = false
                                }
                                RcloneAuthActivity.clearToken()
                                return@collect
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    if (isAuthInProgress) {
                        isAuthInProgress = false
                        authStatus = "Timeout waiting for token."
                        RcloneAuthActivity.clearToken()
                    }
                }
            }
        }
    }

    val cloudTypes = listOf(
        "drive" to "Google Drive",
        "onedrive" to "OneDrive"
    )

    AlertDialog(
        onDismissRequest = { if (!isAuthInProgress) onDismiss() },
        title = {
            Text(
                stringResource(R.string.cloud_backup_settings_title),
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
                    Icon(
                        if (isSignedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isSignedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isSignedIn) stringResource(R.string.cloud_backup_signed_in, accountEmail ?: remoteName)
                        else stringResource(R.string.cloud_backup_not_signed_in),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!isSignedIn) {
                    if (isAuthInProgress) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(authStatus, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                isAuthInProgress = false
                                authStatus = ""
                                RcloneAuthActivity.clearToken()
                            }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    } else if (!showRemoteSetup) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = cloudTypes.find { it.first == selectedRemoteType }?.second ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.cloud_provider)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .heightIn(max = 260.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    cloudTypes.forEach { (type, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedRemoteType = type
                                                expanded = false
                                                showRemoteSetup = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = customRemoteName,
                            onValueChange = { customRemoteName = it },
                            label = { Text("Remote Name") },
                            placeholder = { Text("elecpilot") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val finalName = customRemoteName.ifBlank { "elecpilot" }
                                remoteName = finalName
                                RcloneDriveService.setRemoteName(context, finalName)

                                isAuthInProgress = true
                                authStatus = "Starting ${cloudTypes.find { it.first == selectedRemoteType }?.second ?: "cloud"}..."

                                val authIntent = Intent(context, RcloneAuthActivity::class.java)
                                authIntent.putExtra(RcloneAuthActivity.EXTRA_REMOTE_TYPE, selectedRemoteType)
                                authIntent.putExtra(RcloneAuthActivity.EXTRA_CONFIG_FILE, RcloneDriveService.materializeConfig(context.applicationContext))
                                authIntent.putExtra(RcloneAuthActivity.EXTRA_RCLONE_BINARY, context.applicationContext.applicationInfo.nativeLibraryDir + "/librclone.so")
                                pendingAuthRemoteType = selectedRemoteType
                                pendingAuthRemoteName = finalName
                                authLauncher.launch(authIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Connect ${cloudTypes.find { it.first == selectedRemoteType }?.second ?: "Cloud"}")
                        }
                        TextButton(onClick = { showRemoteSetup = false }) {
                            Text("Cancel")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    RcloneDriveService.signOut(context)
                                }
                                isSignedIn = false
                                accountEmail = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.cloud_backup_sign_out))
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.cloud_backup_enable),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = prefs.cloudEnabled,
                        onCheckedChange = { enabled ->
                            prefs = prefs.copy(cloudEnabled = enabled)
                            context.saveCloudBackupPreferences(prefs)
                            if (enabled) {
                                CloudBackupScheduler.schedulePeriodic(
                                    context,
                                    prefs.cloudFrequencyHours,
                                    prefs.cloudWifiOnly,
                                    prefs.cloudMeteredAllowed,
                                    prefs.cloudChargingOnly
                                )
                            } else {
                                CloudBackupScheduler.cancelPeriodic(context)
                            }
                        }
                    )
                }

                if (prefs.cloudEnabled) {
                    HorizontalDivider()

                    Text(
                        stringResource(R.string.cloud_backup_frequency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val frequencies = listOf(1, 3, 6, 12, 24, 48, 72, 168)
                    var sliderIndex by remember {
                        mutableFloatStateOf(
                            frequencies.indexOf(prefs.cloudFrequencyHours).coerceAtLeast(0).toFloat()
                        )
                    }
                    Text(
                        formatCloudFrequency(context, frequencies[sliderIndex.roundToInt()]),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Slider(
                        value = sliderIndex,
                        onValueChange = { sliderIndex = it.roundToInt().toFloat() },
                        onValueChangeFinished = {
                            val hours = frequencies[sliderIndex.roundToInt()]
                            prefs = prefs.copy(cloudFrequencyHours = hours)
                            context.saveCloudBackupPreferences(prefs)
                            CloudBackupScheduler.schedulePeriodic(
                                context, hours, prefs.cloudWifiOnly, prefs.cloudMeteredAllowed, prefs.cloudChargingOnly
                            )
                        },
                        valueRange = 0f..(frequencies.size - 1).toFloat(),
                        steps = frequencies.size - 2
                    )

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.cloud_backup_format),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.cloudFormat == BackupFormat.CSV,
                            onClick = {
                                prefs = prefs.copy(cloudFormat = BackupFormat.CSV)
                                context.saveCloudBackupPreferences(prefs)
                            },
                            label = { Text("CSV") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = prefs.cloudFormat == BackupFormat.EXCEL,
                            onClick = {
                                prefs = prefs.copy(cloudFormat = BackupFormat.EXCEL)
                                context.saveCloudBackupPreferences(prefs)
                            },
                            label = { Text("Excel") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Text(
                        stringResource(R.string.cloud_backup_constraints),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.cloud_backup_wifi_only),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = prefs.cloudWifiOnly,
                            onCheckedChange = { wifiOnly ->
                                prefs = prefs.copy(cloudWifiOnly = wifiOnly)
                                context.saveCloudBackupPreferences(prefs)
                                CloudBackupScheduler.rescheduleIfNeeded(context)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.cloud_backup_metered),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = prefs.cloudMeteredAllowed,
                            onCheckedChange = { metered ->
                                prefs = prefs.copy(cloudMeteredAllowed = metered)
                                context.saveCloudBackupPreferences(prefs)
                                CloudBackupScheduler.rescheduleIfNeeded(context)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.cloud_backup_charging),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = prefs.cloudChargingOnly,
                            onCheckedChange = { charging ->
                                prefs = prefs.copy(cloudChargingOnly = charging)
                                context.saveCloudBackupPreferences(prefs)
                                CloudBackupScheduler.rescheduleIfNeeded(context)
                            }
                        )
                    }

                    HorizontalDivider()

                    if (prefs.lastCloudBackupTime > 0) {
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(prefs.lastCloudBackupTime))
                        val statusText = when (prefs.lastSyncStatus) {
                            SyncStatus.SUCCESS -> stringResource(R.string.cloud_backup_status_success)
                            SyncStatus.FAILED -> stringResource(R.string.cloud_backup_status_failed)
                            SyncStatus.IN_PROGRESS -> stringResource(R.string.cloud_backup_status_in_progress)
                            SyncStatus.NONE -> ""
                        }
                        Text(
                            stringResource(R.string.cloud_backup_last_backup, dateStr) +
                                    if (statusText.isNotBlank()) " ($statusText)" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (prefs.lastSyncStatus) {
                                SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                                SyncStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (prefs.lastCloudBackupFiles > 0) {
                            Text(
                                stringResource(R.string.cloud_backup_files_uploaded, prefs.lastCloudBackupFiles),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onBackupNow,
                            modifier = Modifier.weight(1f),
                            enabled = isSignedIn
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Cloud,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.cloud_backup_now))
                            }
                        }
                        OutlinedButton(
                            onClick = onBrowse,
                            modifier = Modifier.weight(1f),
                            enabled = isSignedIn
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FolderShared,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.cloud_backup_browse))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isAuthInProgress
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun formatCloudFrequency(context: android.content.Context, hours: Int): String = when {
    hours < 24 -> context.getString(R.string.cloud_backup_freq_hours, hours)
    hours == 24 -> context.getString(R.string.cloud_backup_freq_days, 1)
    else -> context.getString(R.string.cloud_backup_freq_days, hours / 24)
}
