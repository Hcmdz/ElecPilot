package com.HcmDz.ElecPilot.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object UpdateManager {

    private const val API_URL = "https://api.github.com/repos/Hcmdz/ElecPilot/releases/latest"
    private const val PREFS_NAME = "update_prefs"
    private const val KEY_LAST_CHECK = "last_check_timestamp"
    private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L

    data class UpdateInfo(
        val versionTag: String,
        val versionName: String,
        val downloadUrl: String,
        val fileName: String,
        val fileSize: Long,
        val releaseNotes: String,
        val sha256: String? = null
    )

    sealed class UpdateResult {
        data class Found(val info: UpdateInfo) : UpdateResult()
        data object UpToDate : UpdateResult()
        data object Error : UpdateResult()
    }

    @Volatile
    var downloadJob: Job? = null

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun shouldAutoCheck(context: Context): Boolean {
        val lastCheck = prefs(context).getLong(KEY_LAST_CHECK, 0)
        return System.currentTimeMillis() - lastCheck > CHECK_INTERVAL_MS
    }

    fun recordCheck(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    suspend fun checkForUpdate(currentVersionName: String): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "ElecPilot-Android")
                .build()
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) return@withContext UpdateResult.Error
                val body = it.body.string()
                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "")
                val remoteVersion = tagName.removePrefix("v")
                if (!isVersionNewer(currentVersionName, remoteVersion)) return@withContext UpdateResult.UpToDate
                val assets = json.optJSONArray("assets") ?: return@withContext UpdateResult.UpToDate
                val apk = (0 until assets.length())
                    .map { assets.getJSONObject(it) }
                    .firstOrNull { it.optString("name", "").endsWith(".apk") }
                    ?: return@withContext UpdateResult.UpToDate
                UpdateResult.Found(
                    UpdateInfo(
                        versionTag = tagName,
                        versionName = remoteVersion,
                        downloadUrl = apk.getString("browser_download_url"),
                        fileName = apk.getString("name"),
                        fileSize = apk.optLong("size", 0),
                        releaseNotes = json.optString("body", ""),
                        sha256 = Regex("SHA-256:\\s*([a-fA-F0-9]{64})")
                            .find(json.optString("body", ""))
                            ?.groupValues?.get(1)
                    )
                )
            }
        } catch (_: Exception) {
            UpdateResult.Error
        }
    }

    private fun isVersionNewer(current: String, remote: String): Boolean {
        val curParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val remParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(curParts.size, remParts.size)
        for (i in 0 until maxLen) {
            val c = curParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    suspend fun downloadApk(
        context: Context,
        url: String,
        fileName: String,
        expectedSha256: String? = null,
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) return@withContext null
                val body = it.body
                val totalSize = body.contentLength()
                val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
                updateDir.listFiles()?.forEach { it.delete() }
                val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val apkFile = File(updateDir, safeName)
                val digest = MessageDigest.getInstance("SHA-256")
                body.byteStream().use { input ->
                    apkFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var downloaded = 0L
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            digest.update(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            if (totalSize > 0) {
                                withContext(Dispatchers.Main) {
                                    onProgress((downloaded.toFloat() / totalSize).coerceIn(0f, 1f))
                                }
                            }
                        }
                    }
                }
                if (totalSize > 0 && apkFile.length() != totalSize) {
                    apkFile.delete()
                    return@withContext null
                }
                if (expectedSha256 != null) {
                    val computed = digest.digest().joinToString("") { "%02x".format(it) }
                    if (!computed.equals(expectedSha256, ignoreCase = true)) {
                        apkFile.delete()
                        return@withContext null
                    }
                }
                withContext(Dispatchers.Main) { onProgress(1f) }
                apkFile
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
