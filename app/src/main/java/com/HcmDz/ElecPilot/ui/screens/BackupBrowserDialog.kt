package com.HcmDz.ElecPilot.ui.screens

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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
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
import com.HcmDz.ElecPilot.util.BackupFileInfo
import com.HcmDz.ElecPilot.util.BackupManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupBrowserDialog(
    onDismiss: () -> Unit,
    onRestore: (BackupFileInfo) -> Unit,
    isRestoring: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var files by remember { mutableStateOf<List<BackupFileInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInfoFor by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showDeleteFor by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showRenameFor by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showRestoreFor by remember { mutableStateOf<BackupFileInfo?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedKeys by remember { mutableStateOf(setOf<String>()) }
    var showBatchDelete by remember { mutableStateOf(false) }

    val loadFiles: () -> Unit = {
        scope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                files = BackupManager.getBackupFilesList(context)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadFiles() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (isSelectionMode) {
                Column {
                    Text(
                        stringResource(R.string.backup_selected_count, selectedKeys.size),
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            selectedKeys = files.map { "${it.name}_${it.isInternal}" }.toSet()
                        }) { Text(stringResource(R.string.backup_select_all)) }
                        TextButton(onClick = { selectedKeys = emptySet() }) {
                            Text(stringResource(R.string.backup_deselect_all))
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.backup_browse),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.backup_browse), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (files.isEmpty()) {
                Text(
                    stringResource(R.string.backup_no_files),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(min = 200.dp, max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(files, key = { "${it.name}_${it.isInternal}" }) { file ->
                        val key = "${file.name}_${file.isInternal}"
                        BackupFileItem(
                            file = file,
                            isSelectionMode = isSelectionMode,
                            isSelected = key in selectedKeys,
                            onTap = {
                                if (isSelectionMode) {
                                    selectedKeys = if (key in selectedKeys) selectedKeys - key
                                    else selectedKeys + key
                                } else {
                                    showInfoFor = file
                                }
                            },
                            onLongPress = {
                                if (!isSelectionMode && !isRestoring) {
                                    isSelectionMode = true
                                    selectedKeys = setOf(key)
                                }
                            },
                            onRestore = { showRestoreFor = file },
                            onRename = { showRenameFor = file },
                            onDelete = { showDeleteFor = file },
                            enabled = !isRestoring && showRestoreFor == null
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Text(stringResource(R.string.backup_delete_selected))
                    }
                }
            }
        }
    )

    showInfoFor?.let { file ->
        FileInfoDialog(
            file = file,
            onDismiss = { showInfoFor = null }
        )
    }

    showDeleteFor?.let { file ->
        DeleteConfirmDialog(
            file = file,
            onConfirm = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        BackupManager.deleteBackupFile(context, file)
                    }
                    loadFiles()
                    showDeleteFor = null
                }
            },
            onDismiss = { showDeleteFor = null }
        )
    }

    showRenameFor?.let { file ->
        RenameDialog(
            file = file,
            onConfirm = { newName ->
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        BackupManager.renameBackupFile(context, file, newName)
                    }
                    if (ok) {
                        loadFiles()
                    }
                    showRenameFor = null
                }
            },
            onDismiss = { showRenameFor = null }
        )
    }

    showRestoreFor?.let { file ->
        RestoreConfirmDialog(
            file = file,
            onConfirm = {
                onRestore(file)
                showRestoreFor = null
            },
            onDismiss = { showRestoreFor = null }
        )
    }

    if (showBatchDelete) {
        val filesToDelete = files.filter { "${it.name}_${it.isInternal}" in selectedKeys }
        BatchDeleteConfirmDialog(
            files = filesToDelete,
            onConfirm = {
                showBatchDelete = false
                scope.launch {
                    withContext(Dispatchers.IO) {
                        BackupManager.deleteBackupFiles(context, filesToDelete)
                    }
                    loadFiles()
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
private fun BackupFileItem(
    file: BackupFileInfo,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onRestore: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true
) {
    val isExcel = file.name.endsWith(".xlsx", ignoreCase = true)
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(file.lastModified))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.combinedClickable(
                        onClick = { },
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
                "${BackupManager.formatSize(file.size)} · $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!isSelectionMode) {
            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp), enabled = enabled) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = stringResource(R.string.backup_restore),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.backup_rename),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.backup_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
@Suppress("NonObservableLocale")
private fun FileInfoDialog(
    file: BackupFileInfo,
    onDismiss: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        .format(Date(file.lastModified))
    val location = if (file.isInternal) {
        stringResource(R.string.backup_internal_storage)
    } else "SAF"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.backup_info), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(stringResource(R.string.backup_format), file.name.substringAfterLast('.', "").uppercase())
                InfoRow(stringResource(R.string.backup_location), location)
                InfoRow(stringResource(R.string.backup_info_date), dateStr)
                InfoRow(stringResource(R.string.backup_info_size), BackupManager.formatSize(file.size))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.trimEnd(':') + ": ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    file: BackupFileInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.backup_delete), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.backup_delete_confirm))
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
                Text(stringResource(R.string.backup_delete))
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
private fun RestoreConfirmDialog(
    file: BackupFileInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.backup_restore), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.backup_restore_confirm))
                Spacer(Modifier.height(8.dp))
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.backup_restore))
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
private fun RenameDialog(
    file: BackupFileInfo,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentName = file.name.substringBeforeLast('.', "")
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.backup_rename_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.backup_rename_title) + ":",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.filter { c -> c != '/' && c != '\\' && c != '*' && c != '?' && c != ':' && c != '"' && c != '<' && c != '>' && c != '|' } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    ".${file.name.substringAfterLast('.', "")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (newName.isNotBlank()) onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text(stringResource(R.string.backup_rename))
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
private fun BatchDeleteConfirmDialog(
    files: List<BackupFileInfo>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.backup_delete), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.backup_delete_batch_confirm, files.size))
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
                Text(stringResource(R.string.backup_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
