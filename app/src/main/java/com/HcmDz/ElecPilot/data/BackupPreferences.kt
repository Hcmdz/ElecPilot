package com.HcmDz.ElecPilot.data

import android.content.Context
import android.content.SharedPreferences

data class BackupPreferences(
    val enabled: Boolean = false,
    val frequencyHours: Int = 6,
    val format: BackupFormat = BackupFormat.CSV,
    val modules: BackupModules = BackupModules.BOTH,
    val fileMode: BackupFileMode = BackupFileMode.OVERWRITE,
    val maxFiles: Int = 10,
    val lastBackupTime: Long = 0L,
    val backupTreeUri: String = ""
)

enum class BackupFormat { CSV, EXCEL }
enum class BackupModules { DEPARTS, PLC, BOTH }
enum class BackupFileMode { OVERWRITE, TIMESTAMPED }

object BackupPrefsKeys {
    const val PREFS_NAME = "backup_settings"
    const val KEY_ENABLED = "backup_enabled"
    const val KEY_FREQUENCY = "backup_frequency_hours"
    const val KEY_FORMAT = "backup_format"
    const val KEY_MODULES = "backup_modules"
    const val KEY_FILE_MODE = "backup_file_mode"
    const val KEY_MAX_FILES = "backup_max_files"
    const val KEY_LAST_TIME = "backup_last_time"
    const val KEY_TREE_URI = "backup_tree_uri"
}

fun Context.getBackupPreferences(): BackupPreferences {
    val prefs = getSharedPreferences(BackupPrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
    return BackupPreferences(
        enabled = prefs.getBoolean(BackupPrefsKeys.KEY_ENABLED, false),
        frequencyHours = prefs.getInt(BackupPrefsKeys.KEY_FREQUENCY, 6),
        format = try {
            BackupFormat.valueOf(prefs.getString(BackupPrefsKeys.KEY_FORMAT, "CSV") ?: "CSV")
        } catch (_: Exception) { BackupFormat.CSV },
        modules = try {
            BackupModules.valueOf(prefs.getString(BackupPrefsKeys.KEY_MODULES, "BOTH") ?: "BOTH")
        } catch (_: Exception) { BackupModules.BOTH },
        fileMode = try {
            BackupFileMode.valueOf(prefs.getString(BackupPrefsKeys.KEY_FILE_MODE, "OVERWRITE") ?: "OVERWRITE")
        } catch (_: Exception) { BackupFileMode.OVERWRITE },
        maxFiles = prefs.getInt(BackupPrefsKeys.KEY_MAX_FILES, 10),
        lastBackupTime = prefs.getLong(BackupPrefsKeys.KEY_LAST_TIME, 0L),
        backupTreeUri = prefs.getString(BackupPrefsKeys.KEY_TREE_URI, "") ?: ""
    )
}

fun Context.saveBackupPreferences(prefs: BackupPreferences) {
    getSharedPreferences(BackupPrefsKeys.PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
        putBoolean(BackupPrefsKeys.KEY_ENABLED, prefs.enabled)
        putInt(BackupPrefsKeys.KEY_FREQUENCY, prefs.frequencyHours)
        putString(BackupPrefsKeys.KEY_FORMAT, prefs.format.name)
        putString(BackupPrefsKeys.KEY_MODULES, prefs.modules.name)
        putString(BackupPrefsKeys.KEY_FILE_MODE, prefs.fileMode.name)
        putInt(BackupPrefsKeys.KEY_MAX_FILES, prefs.maxFiles)
        putLong(BackupPrefsKeys.KEY_LAST_TIME, prefs.lastBackupTime)
        putString(BackupPrefsKeys.KEY_TREE_URI, prefs.backupTreeUri)
        apply()
    }
}
