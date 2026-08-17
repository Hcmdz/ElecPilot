package com.HcmDz.ElecPilot.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.HcmDz.ElecPilot.data.BackupFileMode
import com.HcmDz.ElecPilot.data.BackupFormat
import com.HcmDz.ElecPilot.data.BackupModules
import com.HcmDz.ElecPilot.data.BackupPreferences
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.data.db.PlcEntity
import com.HcmDz.ElecPilot.data.getBackupPreferences
import com.HcmDz.ElecPilot.data.saveBackupPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }

    fun getBackupDir(context: Context): File {
        return File(context.filesDir, "backups").also { it.mkdirs() }
    }

    fun getTreeDocumentFile(context: Context, uriString: String): DocumentFile? {
        if (uriString.isBlank()) return null
        return try {
            val treeUri = Uri.parse(uriString)
            DocumentFile.fromTreeUri(context, treeUri)
        } catch (_: Exception) {
            null
        }
    }

    private fun isTreeValid(doc: DocumentFile?): Boolean {
        return doc != null && doc.exists() && doc.isDirectory
    }

    suspend fun performBackup(
        context: Context,
        motors: List<MotorEntity>,
        plcList: List<PlcEntity>
    ): BackupResult = withContext(Dispatchers.IO) {
        val prefs = context.getBackupPreferences()
        val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
        val useSaf = isTreeValid(treeDoc)

        try {
            var motorCount = 0
            var plcCount = 0

            if (prefs.modules != BackupModules.PLC && motors.isNotEmpty()) {
                val fileName = generateFilename("departs", prefs.format, prefs.fileMode)
                val writeOk = if (useSaf) {
                    writeCsvOrExcelSaf(context, treeDoc!!, motors = motors, plcList = null, fileName, prefs.format)
                } else {
                    val file = File(getBackupDir(context), fileName)
                    FileOutputStream(file).use { fos ->
                        when (prefs.format) {
                            BackupFormat.CSV -> ExcelUtil.exportToCsvStream(fos, motors)
                            BackupFormat.EXCEL -> ExcelUtil.exportToExcelStream(fos, motors)
                        }
                    }
                    true
                }
                if (writeOk) motorCount = motors.size
            }

            if (prefs.modules != BackupModules.DEPARTS && plcList.isNotEmpty()) {
                val fileName = generateFilename("plc", prefs.format, prefs.fileMode)
                val writeOk = if (useSaf) {
                    writeCsvOrExcelSaf(context, treeDoc!!, motors = null, plcList = plcList, fileName, prefs.format)
                } else {
                    val file = File(getBackupDir(context), fileName)
                    FileOutputStream(file).use { fos ->
                        when (prefs.format) {
                            BackupFormat.CSV -> ExcelUtil.exportPlcToCsvStream(fos, plcList)
                            BackupFormat.EXCEL -> ExcelUtil.exportPlcToExcelStream(fos, plcList)
                        }
                    }
                    true
                }
                if (writeOk) plcCount = plcList.size
            }

            if (motorCount > 0 || plcCount > 0) {
                try {
                    if (prefs.fileMode == BackupFileMode.TIMESTAMPED) {
                        cleanupOldBackups(getBackupDir(context), prefs)
                        if (isTreeValid(treeDoc)) {
                            cleanupOldBackupsSaf(treeDoc!!, prefs)
                        }
                    }
                    cleanupStaleModeFiles(getBackupDir(context), prefs)
                    if (isTreeValid(treeDoc)) {
                        cleanupStaleModeFilesSaf(treeDoc!!, prefs)
                    }
                } catch (_: Exception) { }
            }

            context.saveBackupPreferences(prefs.copy(lastBackupTime = System.currentTimeMillis()))

            val hasData = (prefs.modules != BackupModules.PLC && motors.isNotEmpty()) ||
                    (prefs.modules != BackupModules.DEPARTS && plcList.isNotEmpty())
            if (hasData && motorCount == 0 && plcCount == 0) {
                BackupResult.Error("All writes failed")
            } else {
                BackupResult.Success(motorCount, plcCount)
            }
        } catch (e: Exception) {
            android.util.Log.e("BackupManager", "Backup failed", e)
            BackupResult.Error("Backup failed")
        }
    }

    private fun writeCsvOrExcelSaf(
        context: Context,
        treeDoc: DocumentFile,
        motors: List<MotorEntity>?,
        plcList: List<PlcEntity>?,
        fileName: String,
        format: BackupFormat
    ): Boolean {
        val mimeType = when (format) {
            BackupFormat.CSV -> "text/csv"
            BackupFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        val fileDoc = treeDoc.createFile(mimeType, fileName) ?: return false
        val stream = context.contentResolver.openOutputStream(fileDoc.uri) ?: return false
        try {
            stream.use { os ->
                when (format) {
                    BackupFormat.CSV -> {
                        if (motors != null) ExcelUtil.exportToCsvStream(os, motors)
                        if (plcList != null) ExcelUtil.exportPlcToCsvStream(os, plcList)
                    }
                    BackupFormat.EXCEL -> {
                        if (motors != null) ExcelUtil.exportToExcelStream(os, motors)
                        if (plcList != null) ExcelUtil.exportPlcToExcelStream(os, plcList)
                    }
                }
            }
        } catch (e: Exception) {
            fileDoc.delete()
            return false
        }
        treeDoc.findFile(fileName)?.let { existing ->
            if (existing.uri != fileDoc.uri) existing.delete()
        }
        return true
    }

    fun generateFilename(
        module: String,
        format: BackupFormat,
        fileMode: BackupFileMode
    ): String {
        val ext = when (format) {
            BackupFormat.CSV -> "csv"
            BackupFormat.EXCEL -> "xlsx"
        }
        return when (fileMode) {
            BackupFileMode.OVERWRITE -> "${module}_backup.$ext"
            BackupFileMode.TIMESTAMPED -> {
                val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                "${module}_backup_${df.format(Date())}.$ext"
            }
        }
    }

    private fun cleanupOldBackups(dir: File, prefs: BackupPreferences) {
        val files = dir.listFiles()
            ?.filter { it.isFile && (it.name.endsWith(".csv") || it.name.endsWith(".xlsx")) && it.name.contains("_backup_") }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (files.size > prefs.maxFiles) {
            files.drop(prefs.maxFiles).forEach { it.delete() }
        }
    }

    private fun cleanupOldBackupsSaf(treeDoc: DocumentFile, prefs: BackupPreferences) {
        val files = treeDoc.listFiles()
            .filter { it.isFile && (it.name?.endsWith(".csv") == true || it.name?.endsWith(".xlsx") == true) && (it.name?.contains("_backup_") == true) }
            .sortedByDescending { it.lastModified() }

        if (files.size > prefs.maxFiles) {
            files.drop(prefs.maxFiles).forEach { it.delete() }
        }
    }

    private fun cleanupStaleModeFiles(dir: File, prefs: BackupPreferences) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (!file.isFile) continue
            val name = file.name
            val isOverwrite = name.matches(Regex(".+_backup\\.(csv|xlsx)"))
            val isTimestamped = name.matches(Regex(".+_backup_\\d{8}_\\d{6}\\.(csv|xlsx)"))
            if (prefs.fileMode == BackupFileMode.TIMESTAMPED && isOverwrite) {
                file.delete()
            } else if (prefs.fileMode == BackupFileMode.OVERWRITE && isTimestamped) {
                file.delete()
            }
        }
    }

    private fun cleanupStaleModeFilesSaf(treeDoc: DocumentFile, prefs: BackupPreferences) {
        for (file in treeDoc.listFiles()) {
            if (!file.isFile) continue
            val name = file.name ?: continue
            val isOverwrite = name.matches(Regex(".+_backup\\.(csv|xlsx)"))
            val isTimestamped = name.matches(Regex(".+_backup_\\d{8}_\\d{6}\\.(csv|xlsx)"))
            if (prefs.fileMode == BackupFileMode.TIMESTAMPED && isOverwrite) {
                file.delete()
            } else if (prefs.fileMode == BackupFileMode.OVERWRITE && isTimestamped) {
                file.delete()
            }
        }
    }

    fun getBackupFiles(context: Context): List<File> {
        return getBackupDir(context).listFiles()?.toList() ?: emptyList()
    }

    fun getBackupFilesList(context: Context): List<BackupFileInfo> {
        val prefs = context.getBackupPreferences()
        val seen = mutableMapOf<String, BackupFileInfo>()

        val internalFiles = getBackupDir(context).listFiles()?.filter { it.isFile } ?: emptyList()
        for (f in internalFiles) {
            seen[f.name] = BackupFileInfo(
                name = f.name,
                size = f.length(),
                lastModified = f.lastModified(),
                isInternal = true
            )
        }

        try {
            val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
            if (isTreeValid(treeDoc)) {
                for (doc in treeDoc!!.listFiles()) {
                    if (doc.isFile) {
                        val name = doc.name ?: continue
                        seen[name] = BackupFileInfo(
                            name = name,
                            size = doc.length(),
                            lastModified = doc.lastModified(),
                            isInternal = false
                        )
                    }
                }
            }
        } catch (_: Exception) { }

        return seen.values.sortedByDescending { it.lastModified }
    }

    fun deleteBackupFile(context: Context, fileInfo: BackupFileInfo): Boolean {
        return try {
            if (fileInfo.isInternal) {
                val file = File(getBackupDir(context), fileInfo.name)
                file.delete()
            } else {
                val prefs = context.getBackupPreferences()
                val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
                if (isTreeValid(treeDoc)) {
                    treeDoc!!.findFile(fileInfo.name)?.delete() ?: false
                } else false
            }
        } catch (_: Exception) { false }
    }

    fun deleteBackupFiles(context: Context, files: List<BackupFileInfo>): Int {
        var count = 0
        files.forEach { if (deleteBackupFile(context, it)) count++ }
        return count
    }

    fun renameBackupFile(context: Context, fileInfo: BackupFileInfo, newName: String): Boolean {
        return try {
            if (newName.isBlank()) return false
            if (fileInfo.isInternal) {
                val oldFile = File(getBackupDir(context), fileInfo.name)
                val ext = fileInfo.name.substringAfterLast('.', "")
                val newFile = File(getBackupDir(context), "$newName.$ext")
                if (newFile.exists()) return false
                oldFile.renameTo(newFile)
            } else {
                val prefs = context.getBackupPreferences()
                val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
                if (isTreeValid(treeDoc)) {
                    val ext = fileInfo.name.substringAfterLast('.', "")
                    if (treeDoc!!.findFile("$newName.$ext") != null) return false
                    treeDoc.findFile(fileInfo.name)?.renameTo(newName) ?: false
                } else false
            }
        } catch (_: Exception) { false }
    }

    fun getSpaceUsed(context: Context): Long {
        val prefs = context.getBackupPreferences()
        var total = getBackupDir(context).listFiles()?.sumOf { it.length() } ?: 0L
        val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
        if (isTreeValid(treeDoc)) {
            total += treeDoc!!.listFiles().filter { it.isFile }.sumOf { it.length() }
        }
        return total
    }

    fun getDisplayPath(context: Context, prefs: BackupPreferences): String {
        if (prefs.backupTreeUri.isBlank()) return ""
        return try {
            val treeUri = Uri.parse(prefs.backupTreeUri)
            val doc = DocumentFile.fromTreeUri(context, treeUri)
            if (doc != null && doc.exists() && doc.name != null) {
                doc.name ?: prefs.backupTreeUri
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun getBackupFileUri(context: Context, fileInfo: BackupFileInfo): Uri? {
        return try {
            if (fileInfo.isInternal) {
                val file = File(getBackupDir(context), fileInfo.name)
                if (file.exists()) Uri.fromFile(file) else null
            } else {
                val prefs = context.getBackupPreferences()
                val treeDoc = getTreeDocumentFile(context, prefs.backupTreeUri)
                if (isTreeValid(treeDoc)) {
                    treeDoc!!.findFile(fileInfo.name)?.uri
                } else null
            }
        } catch (_: Exception) { null }
    }
}

sealed class BackupResult {
    data class Success(val motorCount: Int, val plcCount: Int) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

data class BackupFileInfo(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isInternal: Boolean
)
