package com.HcmDz.ElecPilot.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.CloudBackupFileInfo
import com.HcmDz.ElecPilot.data.getCloudBackupPreferences
import com.HcmDz.ElecPilot.util.CloudBackupManager
import com.HcmDz.ElecPilot.util.NotificationHelper
import com.HcmDz.ElecPilot.util.BackupManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CloudBackupBrowserDialog(
    onDismiss: () -> Unit,
    onRestore: (CloudBackupFileInfo) -> Unit,
    onCancelRestore: () -> Unit = {},
    isRestoring: Boolean = false,
    restorePhase: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cachedFiles = CloudBackupManager.getCachedFiles()
    var files by remember { mutableStateOf(cachedFiles) }
    var isLoading by remember { mutableStateOf(cachedFiles.isEmpty()) }
    var showDeleteFor by remember { mutableStateOf<CloudBackupFileInfo?>(null) }
    var showRestoreFor by remember { mutableStateOf<CloudBackupFileInfo?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedKeys by remember { mutableStateOf(setOf<String>()) }
    var showBatchDelete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loadFiles: () -> Unit = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val folderName = context.getCloudBackupPreferences().cloudFolderName
                files = CloudBackupManager.listCloudBackups(context, folderName)
            } catch (e: Exception) {
                android.util.Log.e("CloudBackup", "Failed to list files", e)
                files = emptyList()
                errorMessage = context.getString(R.string.cloud_operation_error_generic)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadFiles()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (isSelectionMode) {
                Column {
                    Text(
                        stringResource(R.string.cloud_backup_selected_count, selectedKeys.size),
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            selectedKeys = files.map { it.id }.toSet()
                        }) { Text(stringResource(R.string.cloud_backup_select_all)) }
                        TextButton(onClick = { selectedKeys = emptySet() }) {
                            Text(stringResource(R.string.cloud_backup_deselect_all))
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.cloud_backup_browse),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                if (errorMessage != null) {
                    Text(
                        errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.cloud_backup_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                isRestoring && restorePhase.isNotBlank() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            restorePhase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = onCancelRestore,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.cancel_operation))
                        }
                    }
                }
                files.isEmpty() -> {
                    Text(
                        stringResource(R.string.cloud_backup_no_files),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(min = 200.dp, max = 500.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(files, key = { it.id }) { file ->
                            CloudBackupFileItem(
                                file = file,
                                isSelectionMode = isSelectionMode,
                                isSelected = file.id in selectedKeys,
                                onTap = {
                                    if (isSelectionMode) {
                                        selectedKeys = if (file.id in selectedKeys) selectedKeys - file.id
                                        else selectedKeys + file.id
                                    }
                                },
                                onLongPress = {
                                    if (!isSelectionMode && !isRestoring) {
                                        isSelectionMode = true
                                        selectedKeys = setOf(file.id)
                                    }
                                },
                                onRestore = { showRestoreFor = file },
                                onDelete = { showDeleteFor = file },
                                enabled = !isRestoring && showRestoreFor == null
                            )
                        }
                    }
                }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        CloudBackupManager.forceRefreshCache(context)
                        loadFiles()
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_refresh))
                }
                TextButton(onClick = {
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedKeys = emptySet()
                    } else {
                        onDismiss()
                    }
                }) {
                    Text(stringResource(R.string.close))
                }
                if (isSelectionMode && selectedKeys.isNotEmpty()) {
                    TextButton(
                        onClick = { showBatchDelete = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.cloud_backup_delete_selected))
                    }
                }
            }
        }
    )

    showDeleteFor?.let { file ->
        CloudDeleteConfirmDialog(
            file = file,
            onConfirm = {
                scope.launch {
                    try {
                        CloudBackupManager.deleteCloudBackup(context, file.id)
                        NotificationHelper.showBackupDeleted(context, file.name)
                    } catch (e: Exception) {
                        android.util.Log.e("CloudBackup", "Failed to delete file", e)
                        errorMessage = context.getString(R.string.cloud_operation_error_generic)
                    }
                    loadFiles()
                }
                showDeleteFor = null
            },
            onDismiss = { showDeleteFor = null }
        )
    }

    showRestoreFor?.let { file ->
        CloudRestoreConfirmDialog(
            file = file,
            onConfirm = {
                onRestore(file)
                showRestoreFor = null
            },
            onDismiss = { showRestoreFor = null }
        )
    }

    if (showBatchDelete) {
        val filesToDelete = files.filter { it.id in selectedKeys }
        CloudBatchDeleteConfirmDialog(
            files = filesToDelete,
            onConfirm = {
                showBatchDelete = false
                scope.launch {
                    try {
                        coroutineScope {
                            filesToDelete.map { file ->
                                async {
                                    try {
                                        CloudBackupManager.deleteCloudBackup(context, file.id)
                                    } catch (e: Exception) {
                                        android.util.Log.e("CloudBackup", "Failed to delete file", e)
                                    }
                                }
                            }.awaitAll()
                        }
                        loadFiles()
                        NotificationHelper.showBackupDeleted(context, context.getString(R.string.notif_backup_deleted_batch_body, filesToDelete.size))
                    } catch (e: Exception) {
                        android.util.Log.e("CloudBackup", "Batch delete failed", e)
                        errorMessage = context.getString(R.string.cloud_operation_error_generic)
                    }
                    isSelectionMode = false
                    selectedKeys = emptySet()
                }
            },
            onDismiss = { showBatchDelete = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("NonObservableLocale")
@Composable
private fun CloudBackupFileItem(
    file: CloudBackupFileInfo,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true
) {
    val isExcel = file.name.endsWith(".xlsx", ignoreCase = true)
    val dateStr = remember(file.modifiedTime) {
        if (file.modifiedTime > 0L)
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(file.modifiedTime))
        else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.combinedClickable(
                        onClick = { onTap() },
                        onLongClick = onLongPress
                    )
                } else {
                    Modifier.combinedClickable(
                        onClick = onTap,
                        onLongClick = onLongPress
                    )
                }
            )
            .then(
                if (isSelected)
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onTap() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            if (isExcel) Icons.Default.TableChart else Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isExcel) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    append(BackupManager.formatSize(file.size))
                    if (dateStr.isNotEmpty()) {
                        append(" · ")
                        append(dateStr)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!isSelectionMode) {
            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp), enabled = enabled) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = stringResource(R.string.cloud_backup_restore),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cloud_backup_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CloudDeleteConfirmDialog(
    file: CloudBackupFileInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.cloud_backup_delete), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.cloud_backup_delete_confirm))
                Spacer(Modifier.height(8.dp))
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.cloud_backup_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun CloudRestoreConfirmDialog(
    file: CloudBackupFileInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.cloud_backup_restore), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.cloud_backup_restore_confirm))
                Spacer(Modifier.height(8.dp))
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.cloud_backup_restore_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.cloud_backup_restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun CloudBatchDeleteConfirmDialog(
    files: List<CloudBackupFileInfo>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.cloud_backup_delete), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.cloud_backup_delete_batch_confirm, files.size))
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(files) { file ->
                        Text(
                            "• ${file.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.cloud_backup_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
