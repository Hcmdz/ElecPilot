package com.HcmDz.ElecPilot.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.HcmDz.ElecPilot.data.CloudBackupFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun isStaleTempFile(name: String): Boolean =
    name.startsWith("rclone_conf_") || name.startsWith("cloud_restore_") ||
        name.startsWith("download_") || name.startsWith("upload_")

object RcloneDriveService {

    private const val PREFS_NAME = "cloud_backup_rclone"
    private const val KEY_REMOTE_NAME = "remote_name"
    private const val KEY_ACCOUNT_EMAIL = "account_email"
    private const val KEY_SIGNED_IN = "signed_in"

    private var rcloneBinary: String = ""
    private var confStorePath: String = ""
    private var appContext: Context? = null

    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        rcloneBinary = ctx.applicationInfo.nativeLibraryDir + "/librclone.so"
        confStorePath = File(ctx.filesDir, "rclone.conf.enc").absolutePath
        migrateLegacyConfig(ctx)
        cleanupStaleTempFiles(ctx)

        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_REMOTE_NAME)) {
            prefs.edit().putString(KEY_REMOTE_NAME, "elecpilot").apply()
        }
    }

    private fun migrateLegacyConfig(context: Context) {
        val plain = File(context.filesDir, "rclone.conf")
        if (plain.exists() && !File(confStorePath).exists()) {
            try {
                val content = plain.readText()
                if (content.isNotBlank()) {
                    CryptoManager.ensureKey()
                    File(confStorePath).writeText(CryptoManager.encrypt(content))
                }
                plain.delete()
            } catch (_: Exception) {}
        }
    }

    private fun cleanupStaleTempFiles(ctx: Context) {
        ctx.cacheDir.listFiles()
            ?.filter { it.isFile && isStaleTempFile(it.name) }
            ?.forEach { it.delete() }
    }

    private fun readConfigContent(): String {
        val file = File(confStorePath)
        return if (!file.exists()) "" else try {
            CryptoManager.decrypt(file.readText())
        } catch (_: Exception) { "" }
    }

    private fun writeConfigContent(content: String) {
        if (content.isBlank()) {
            File(confStorePath).delete()
            return
        }
        CryptoManager.ensureKey()
        File(confStorePath).writeText(CryptoManager.encrypt(content))
    }

    fun materializeConfig(context: Context): String {
        val temp = File(context.cacheDir, "rclone_conf_${java.util.UUID.randomUUID().toString().take(8)}.tmp")
        temp.writeText(readConfigContent())
        temp.setReadable(true, true)
        temp.setWritable(false, false)
        return temp.absolutePath
    }

    fun deleteConfigTemp(path: String) {
        try { File(path).delete() } catch (_: Exception) {}
    }

    fun isInitialized(): Boolean = rcloneBinary.isNotEmpty() && File(rcloneBinary).exists()

    fun isSignedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SIGNED_IN, false)
    }

    private fun setSignedIn(context: Context, signedIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SIGNED_IN, signedIn).apply()
    }

    fun getRemoteName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REMOTE_NAME, "elecpilot") ?: "elecpilot"
    }

    fun setRemoteName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_REMOTE_NAME, name).apply()
    }

    fun getAccountEmail(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCOUNT_EMAIL, null)
    }

    fun saveAccountEmail(context: Context, email: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACCOUNT_EMAIL, email).apply()
    }

    suspend fun listRemotes(): List<String> {
        val ctx = appContext ?: return emptyList()
        val result = runRcloneCommand(ctx, "listremotes")
        return result.output.map { it.trimEnd(':').trim() }.filter { it.isNotEmpty() }
    }

    suspend fun listFiles(context: Context, folderName: String): List<CloudBackupFileInfo> = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$folderName/"

        val result = runRcloneCommand(
            context,
            "lsf", remotePath,
            "--format", "tsp",
            "--separator", "\t"
        )

        if (!result.isSuccess) {
            android.util.Log.e("Rclone", "listFiles failed")
            throw Exception("Cloud operation failed")
        }

        result.output.mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size >= 3) {
                val rawPath = parts[2]
                val isDirectory = rawPath.endsWith("/")
                CloudBackupFileInfo(
                    id = folderName + "/" + rawPath.trimEnd('/'),
                    name = rawPath.trimEnd('/'),
                    size = if (isDirectory) -1L else (parts[1].toLongOrNull() ?: 0L),
                    modifiedTime = parseTime(parts[0]),
                    mimeType = guessMimeType(rawPath),
                    webViewLink = null
                )
            } else null
        }
    }

    suspend fun uploadFile(
        context: Context,
        inputStream: java.io.InputStream,
        fileName: String,
        mimeType: String,
        folderName: String,
        onProgress: ((percent: Float, speedBytesPerSec: Long, etaSeconds: Long) -> Unit)? = null
    ): CloudBackupFileInfo = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$folderName/"

        val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
        try {
            tempFile.outputStream().use { inputStream.copyTo(it) }
            val fileSize = tempFile.length()
            val remoteDest = "$remote:/$folderName/$fileName"

            val result = runRcloneCommand(
                context,
                "copyto", tempFile.absolutePath, remoteDest,
                onProgress = if (onProgress != null) { jsonLine ->
                    parseProgressJson(jsonLine)?.let { (p, s, e) -> onProgress(p, s, e) }
                } else null
            )

            if (!result.isSuccess) {
                android.util.Log.e("Rclone", "uploadFile failed")
                throw Exception("Cloud operation failed")
            }

            CloudBackupFileInfo(
                id = "$folderName/$fileName",
                name = fileName,
                size = fileSize,
                modifiedTime = System.currentTimeMillis(),
                mimeType = mimeType,
                webViewLink = null
            )
        } finally {
            tempFile.delete()
        }
    }

    suspend fun deleteFile(context: Context, fileId: String) = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$fileId"

            var result = runRcloneCommand(context, "deletefile", remotePath)
        if (!result.isSuccess) {
            result = runRcloneCommand(context, "delete", "$remotePath/")
        }
        if (!result.isSuccess) {
            android.util.Log.e("Rclone", "deleteFile failed")
            throw Exception("Cloud operation failed")
        }
    }

    suspend fun downloadFile(context: Context, fileId: String, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$fileId"

        val tempFile = File(context.cacheDir, "download_${System.currentTimeMillis()}")
        try {
            val result = runRcloneCommand(
                context,
                "copyto", remotePath, tempFile.absolutePath
            )

            if (!result.isSuccess) {
                android.util.Log.e("Rclone", "downloadFile failed")
                throw Exception("Cloud operation failed")
            }

            tempFile.inputStream().use { input ->
                input.copyTo(outputStream)
            }
        } finally {
            tempFile.delete()
        }
    }

    suspend fun downloadToTempFile(context: Context, fileId: String, onProgress: ((percent: Float, speedBytesPerSec: Long, etaSeconds: Long) -> Unit)? = null): File = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$fileId"
        val tempFile = File(context.cacheDir, "cloud_restore_${System.currentTimeMillis()}.tmp")
        val result = runRcloneCommand(context, "copyto", remotePath, tempFile.absolutePath,
            onProgress = if (onProgress != null) { jsonLine ->
                parseProgressJson(jsonLine)?.let { (p, s, e) -> onProgress(p, s, e) }
            } else null
        )
        if (!result.isSuccess) {
            tempFile.delete()
            android.util.Log.e("Rclone", "downloadToTempFile failed")
            throw Exception("Cloud operation failed")
        }
        tempFile
    }

    suspend fun createFolder(context: Context, folderName: String) = withContext(Dispatchers.IO) {
        val remote = getRemoteName(context)
        val remotePath = "$remote:/$folderName/"

        runRcloneCommand(context, "mkdir", remotePath)
    }

    suspend fun signOut(context: Context) {
        setSignedIn(context, false)
        File(confStorePath).delete()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun saveRemoteConfig(context: Context, remoteType: String, remoteName: String, tokenJson: String) {
        var content = readConfigContent()

        val sectionPattern = Regex("\\[$remoteName\\]\\n.*?(?=\\n\\[|$)", RegexOption.DOT_MATCHES_ALL)
        content = sectionPattern.replace(content, "")

        val newSection = buildString {
            appendLine()
            appendLine("[$remoteName]")
            appendLine("type = $remoteType")
            if (remoteType == "drive") {
                appendLine("scope = drive")
            }
            appendLine("token = $tokenJson")
        }
        content = content.trimEnd() + newSection

        if (remoteType == "onedrive") {
            val tokenObj = org.json.JSONObject(tokenJson)
            val accessToken = tokenObj.getString("access_token")
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/drive")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                throw Exception("Microsoft Graph API error: ${response.code}")
            }
            val body = response.body?.string() ?: throw Exception("Empty response from Microsoft Graph")
            response.close()
            val driveJson = org.json.JSONObject(body)
            val driveId = driveJson.getString("id")
            val driveType = driveJson.getString("driveType")
            val driveAppend = "drive_id = $driveId\ndrive_type = $driveType\n"
            content = content.trimEnd() + "\n" + driveAppend
        }

        writeConfigContent(content)
        saveAccountEmail(context, remoteName)
        setSignedIn(context, true)
    }

    fun openRcloneConfig(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rclone.org/commands/rclone_config/"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private suspend fun runRcloneCommand(context: Context, vararg args: String, onProgress: ((String) -> Unit)? = null): RcloneResult {
        val confPath = materializeConfig(context)
        try {
            val command = mutableListOf(rcloneBinary, "--config", confPath)
            if (onProgress != null && args.isNotEmpty() && args[0] == "copyto") {
                command.add("--stats=1s")
                command.add("--use-json-log")
                command.add("--stats-log-level")
                command.add("NOTICE")
            }
            command.addAll(args)

            return try {
                val process = ProcessBuilder(command)
                    .directory(context.cacheDir)
                    .redirectErrorStream(false)
                    .apply {
                        environment()["TMPDIR"] = context.cacheDir.absolutePath
                        environment()["HOME"] = context.filesDir.parent ?: context.filesDir.absolutePath
                    }
                    .start()

                val stdoutLines = mutableListOf<String>()
                val stderrLines = mutableListOf<String>()

                val stdoutThread = Thread {
                    try {
                        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                line?.let { stdoutLines.add(it) }
                            }
                        }
                    } catch (_: Exception) {}
                }
                val stderrThread = Thread {
                    try {
                        BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                line?.let { l ->
                                    stderrLines.add(l)
                                    if (onProgress != null && l.startsWith("{")) {
                                        onProgress(l)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                stdoutThread.isDaemon = true
                stderrThread.isDaemon = true
                stdoutThread.start()
                stderrThread.start()

                while (process.isAlive) {
                    if (!currentCoroutineContext().isActive) {
                        process.destroyForcibly()
                        throw kotlinx.coroutines.CancellationException("Operation cancelled by user")
                    }
                    val finished = process.waitFor(1, TimeUnit.SECONDS)
                    if (finished) break
                }

                stdoutThread.join(5000)
                stderrThread.join(5000)

                val exitCode = process.exitValue()
                val success = exitCode == 0
                if (!success) {
                    android.util.Log.e("Rclone", "Command failed with exit code $exitCode")
                }
                RcloneResult(success, stdoutLines, stderrLines)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("Rclone", "Process error", e)
                RcloneResult(false, emptyList(), listOf("Rclone process error"))
            }
        } finally {
            deleteConfigTemp(confPath)
        }
    }

    data class RcloneResult(
        val isSuccess: Boolean,
        val output: List<String>,
        val error: List<String>
    )

    private fun parseTime(timeStr: String): Long {
        if (timeStr.isBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.parse(timeStr)?.time ?: 0L
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                sdf.parse(timeStr)?.time ?: 0L
            } catch (_: Exception) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    sdf.parse(timeStr)?.time ?: 0L
                } catch (_: Exception) { 0L }
            }
        }
    }

    private fun guessMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".csv", true) -> "text/csv"
            fileName.endsWith(".xlsx", true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            fileName.endsWith(".xls", true) -> "application/vnd.ms-excel"
            else -> "application/octet-stream"
        }
    }

    private fun parseProgressJson(jsonLine: String): Triple<Float, Long, Long>? {
        return try {
            val json = org.json.JSONObject(jsonLine)
            val stats = json.optJSONObject("stats") ?: return null
            val bytes = stats.optDouble("bytes", 0.0)
            val totalBytes = stats.optDouble("totalBytes", 0.0)
            val percent = if (totalBytes > 0) (bytes / totalBytes * 100).toFloat() else 0f
            val speed = stats.optLong("speed", 0L)
            val eta = stats.optLong("eta", 0L)
            Triple(percent, speed, eta)
        } catch (_: Exception) { null }
    }
}
