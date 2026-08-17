package com.HcmDz.ElecPilot.util

import android.content.Context
import com.HcmDz.ElecPilot.data.BackupFormat
import com.HcmDz.ElecPilot.data.CloudBackupFileInfo
import com.HcmDz.ElecPilot.data.db.AppDatabase
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.data.db.PlcDatabase
import com.HcmDz.ElecPilot.data.db.PlcEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.room.withTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

sealed class CloudBackupResult {
    data class Success(val fileCount: Int, val motorCount: Int, val plcCount: Int) : CloudBackupResult()
    data class Error(val message: String) : CloudBackupResult()
}

sealed class CloudRestoreResult {
    data class Success(val motorsAdded: Int, val plcAdded: Int) : CloudRestoreResult()
    data class Error(val message: String) : CloudRestoreResult()
}

data class BackupProgress(
    val percent: Float,
    val speedBytesPerSec: Long,
    val etaSeconds: Long
)

object CloudBackupManager {

    private val cachedFiles = AtomicReference<List<CloudBackupFileInfo>?>(null)
    private const val DISK_CACHE_TTL_MS = 5 * 60 * 1000L
    private const val DISK_CACHE_FILE = "cloud_file_list_cache.json"
    private const val MAX_RESTORE_BYTES = 50L * 1024 * 1024

    internal fun isRestoreSizeAllowed(size: Long): Boolean =
        size in 1..MAX_RESTORE_BYTES

    fun getCachedFiles(): List<CloudBackupFileInfo> = cachedFiles.get() ?: emptyList()

    fun clearCache() {
        cachedFiles.set(null)
    }

    fun forceRefreshCache(context: Context) {
        cachedFiles.set(null)
        deleteDiskCache(context)
    }

    suspend fun performCloudBackup(
        context: Context,
        motors: List<MotorEntity>,
        plcList: List<PlcEntity>,
        format: BackupFormat,
        folderName: String,
        onProgress: ((BackupProgress) -> Unit)? = null
    ): CloudBackupResult = withContext(Dispatchers.IO) {
        try {
            if (!RcloneDriveService.isSignedIn(context)) {
                return@withContext CloudBackupResult.Error("Not signed in to cloud storage")
            }

            var fileCount = 0
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val isExcel = format == BackupFormat.EXCEL
            val ext = if (isExcel) "xlsx" else "csv"
            val mime = if (isExcel) "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" else "text/csv"

            coroutineScope {
                val progressRelay = if (onProgress != null) { p: Float, s: Long, e: Long ->
                    onProgress(BackupProgress(p, s, e))
                } else null

                val motorJob = async {
                    if (motors.isNotEmpty()) {
                        val motorFileName = "departs_backup_$timestamp.$ext"
                        val motorBytes = exportMotorsToBytes(motors, format)
                        RcloneDriveService.uploadFile(
                            context = context,
                            inputStream = ByteArrayInputStream(motorBytes),
                            fileName = motorFileName,
                            mimeType = mime,
                            folderName = folderName,
                            onProgress = progressRelay
                        )
                        true
                    } else false
                }
                val plcJob = async {
                    if (plcList.isNotEmpty()) {
                        val plcFileName = "plc_backup_$timestamp.$ext"
                        val plcBytes = exportPlcToBytes(plcList, format)
                        RcloneDriveService.uploadFile(
                            context = context,
                            inputStream = ByteArrayInputStream(plcBytes),
                            fileName = plcFileName,
                            mimeType = mime,
                            folderName = folderName,
                            onProgress = progressRelay
                        )
                        true
                    } else false
                }
                if (motorJob.await()) fileCount++
                if (plcJob.await()) fileCount++
            }

            if (fileCount == 0) {
                return@withContext CloudBackupResult.Error("No data to backup")
            }

            clearCache()
            CloudBackupResult.Success(fileCount, motors.size, plcList.size)
        } catch (e: Exception) {
            android.util.Log.e("CloudBackup", "Backup failed", e)
            CloudBackupResult.Error(e.message ?: "Cloud backup failed")
        }
    }

    suspend fun performCloudRestore(
        context: Context,
        fileId: String,
        fileName: String,
        onPhase: (suspend (String) -> Unit)? = null,
        onProgress: ((BackupProgress) -> Unit)? = null
    ): CloudRestoreResult = withContext(Dispatchers.IO) {
        try {
            if (!RcloneDriveService.isSignedIn(context)) {
                return@withContext CloudRestoreResult.Error("Not signed in to cloud storage")
            }

            onPhase?.invoke("Downloading...")
            val tempFile = RcloneDriveService.downloadToTempFile(context, fileId,
                onProgress = if (onProgress != null) { p: Float, s: Long, e: Long ->
                    onProgress(BackupProgress(p, s, e))
                } else null
            )
            try {
                if (tempFile.length() == 0L) {
                    return@withContext CloudRestoreResult.Error("Downloaded file is empty")
                }
                if (!isRestoreSizeAllowed(tempFile.length())) {
                    return@withContext CloudRestoreResult.Error("File too large to restore")
                }

                val fileBytes = tempFile.readBytes()
                val isExcel = fileBytes.size >= 2 &&
                    ((fileBytes[0] == 0x50.toByte() && fileBytes[1] == 0x4B.toByte()))

                var motorsAdded = 0
                var plcAdded = 0

                onPhase?.invoke("Parsing file...")
                when {
                    fileName.contains("departs", ignoreCase = true) -> {
                        val motors = ExcelUtil.importMotorsFromBytes(fileBytes, isExcel)
                        if (motors.isNotEmpty()) {
                            val db = AppDatabase.getInstance(context)
                            db.withTransaction {
                                db.motorDao().deleteAll()
                                db.motorDao().insertAll(motors)
                            }
                            motorsAdded = motors.size
                        }
                    }
                    fileName.contains("plc", ignoreCase = true) -> {
                        val plcs = ExcelUtil.importPlcsFromBytes(fileBytes, isExcel)
                        if (plcs.isNotEmpty()) {
                            val plcDb = PlcDatabase.getInstance(context)
                            plcDb.withTransaction {
                                plcDb.plcDao().deleteAll()
                                plcDb.plcDao().insertAll(plcs)
                            }
                            plcAdded = plcs.size
                        }
                    }
                    else -> {
                        return@withContext CloudRestoreResult.Error("Unknown file type")
                    }
                }

                CloudRestoreResult.Success(motorsAdded, plcAdded)
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e("CloudRestore", "Restore failed", e)
            CloudRestoreResult.Error(e.message ?: "Cloud restore failed")
        }
    }

    suspend fun listCloudBackups(context: Context, folderName: String): List<CloudBackupFileInfo> {
        val memoryCached = cachedFiles.get()
        if (memoryCached != null) return memoryCached

        val diskCached = loadDiskCache(context)
        if (diskCached != null) {
            cachedFiles.set(diskCached)
            return diskCached
        }

        val result = RcloneDriveService.listFiles(context, folderName)
        cachedFiles.set(result)
        saveDiskCache(context, result)
        return result
    }

    suspend fun deleteCloudBackup(context: Context, fileId: String) {
        RcloneDriveService.deleteFile(context, fileId)
        clearCache()
        deleteDiskCache(context)
    }

    suspend fun downloadCloudBackup(context: Context, fileId: String, outputStream: java.io.OutputStream) {
        RcloneDriveService.downloadFile(context, fileId, outputStream)
    }

    private fun exportMotorsToBytes(motors: List<MotorEntity>, format: BackupFormat): ByteArray {
        val baos = ByteArrayOutputStream()
        when (format) {
            BackupFormat.CSV -> ExcelUtil.exportToCsvStream(baos, motors)
            BackupFormat.EXCEL -> ExcelUtil.exportToExcelStream(baos, motors)
        }
        return baos.toByteArray()
    }

    private fun exportPlcToBytes(plcList: List<PlcEntity>, format: BackupFormat): ByteArray {
        val baos = ByteArrayOutputStream()
        when (format) {
            BackupFormat.CSV -> ExcelUtil.exportPlcToCsvStream(baos, plcList)
            BackupFormat.EXCEL -> ExcelUtil.exportPlcToExcelStream(baos, plcList)
        }
        return baos.toByteArray()
    }

    private fun saveDiskCache(context: Context, files: List<CloudBackupFileInfo>) {
        try {
            val jsonArray = JSONArray()
            for (file in files) {
                val obj = JSONObject().apply {
                    put("id", file.id)
                    put("name", file.name)
                    put("size", file.size)
                    put("modifiedTime", file.modifiedTime)
                    put("mimeType", file.mimeType)
                    put("webViewLink", file.webViewLink ?: JSONObject.NULL)
                }
                jsonArray.put(obj)
            }
            val wrapper = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("files", jsonArray)
            }
            CryptoManager.ensureKey()
            val cacheFile = File(context.cacheDir, DISK_CACHE_FILE)
            cacheFile.writeText(CryptoManager.encrypt(wrapper.toString()))
        } catch (e: Exception) {
            android.util.Log.w("CloudBackup", "Failed to save disk cache", e)
        }
    }

    private fun loadDiskCache(context: Context): List<CloudBackupFileInfo>? {
        try {
            val cacheFile = File(context.cacheDir, DISK_CACHE_FILE)
            if (!cacheFile.exists()) return null
            val raw = cacheFile.readText()
            val plain = try { CryptoManager.decrypt(raw) } catch (_: Exception) { raw }
            val wrapper = JSONObject(plain)
            val timestamp = wrapper.optLong("timestamp", 0L)
            if (System.currentTimeMillis() - timestamp > DISK_CACHE_TTL_MS) {
                cacheFile.delete()
                return null
            }
            val jsonArray = wrapper.getJSONArray("files")
            val files = mutableListOf<CloudBackupFileInfo>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val webViewLink = obj.opt("webViewLink")
                files.add(
                    CloudBackupFileInfo(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        size = obj.getLong("size"),
                        modifiedTime = obj.getLong("modifiedTime"),
                        mimeType = obj.getString("mimeType"),
                        webViewLink = if (webViewLink == JSONObject.NULL) null else webViewLink as? String
                    )
                )
            }
            return files
        } catch (e: Exception) {
            android.util.Log.w("CloudBackup", "Failed to load disk cache")
            return null
        }
    }

    private fun deleteDiskCache(context: Context) {
        try {
            File(context.cacheDir, DISK_CACHE_FILE).delete()
        } catch (_: Exception) {}
    }
}
