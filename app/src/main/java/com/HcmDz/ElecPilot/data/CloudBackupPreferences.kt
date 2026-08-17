package com.HcmDz.ElecPilot.data

import android.content.Context

data class CloudBackupPreferences(
    val cloudEnabled: Boolean = false,
    val cloudProvider: CloudProvider = CloudProvider.GOOGLE_DRIVE,
    val cloudFormat: BackupFormat = BackupFormat.CSV,
    val cloudFrequencyHours: Int = 24,
    val cloudWifiOnly: Boolean = true,
    val cloudChargingOnly: Boolean = false,
    val cloudMeteredAllowed: Boolean = false,
    val lastCloudBackupTime: Long = 0L,
    val lastCloudBackupFiles: Int = 0,
    val lastSyncStatus: SyncStatus = SyncStatus.NONE,
    val cloudFolderName: String = "ElecPilot"
)

enum class CloudProvider { GOOGLE_DRIVE, ONEDRIVE }
enum class SyncStatus { NONE, IN_PROGRESS, SUCCESS, FAILED }

object CloudBackupPrefsKeys {
    const val PREFS_NAME = "cloud_backup_settings"
    const val KEY_ENABLED = "cloud_enabled"
    const val KEY_PROVIDER = "cloud_provider"
    const val KEY_FORMAT = "cloud_format"
    const val KEY_FREQUENCY = "cloud_frequency_hours"
    const val KEY_WIFI_ONLY = "cloud_wifi_only"
    const val KEY_CHARGING_ONLY = "cloud_charging_only"
    const val KEY_METERED_ALLOWED = "cloud_metered_allowed"
    const val KEY_LAST_TIME = "cloud_last_backup_time"
    const val KEY_LAST_FILES = "cloud_last_backup_files"
    const val KEY_LAST_STATUS = "cloud_last_sync_status"
    const val KEY_FOLDER_NAME = "cloud_folder_name"
}

fun Context.getCloudBackupPreferences(): CloudBackupPreferences {
    val prefs = getSharedPreferences(CloudBackupPrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
    return CloudBackupPreferences(
        cloudEnabled = prefs.getBoolean(CloudBackupPrefsKeys.KEY_ENABLED, false),
        cloudProvider = try {
            CloudProvider.valueOf(prefs.getString(CloudBackupPrefsKeys.KEY_PROVIDER, "GOOGLE_DRIVE") ?: "GOOGLE_DRIVE")
        } catch (_: Exception) { CloudProvider.GOOGLE_DRIVE },
        cloudFormat = try {
            BackupFormat.valueOf(prefs.getString(CloudBackupPrefsKeys.KEY_FORMAT, "CSV") ?: "CSV")
        } catch (_: Exception) { BackupFormat.CSV },
        cloudFrequencyHours = prefs.getInt(CloudBackupPrefsKeys.KEY_FREQUENCY, 24),
        cloudWifiOnly = prefs.getBoolean(CloudBackupPrefsKeys.KEY_WIFI_ONLY, true),
        cloudChargingOnly = prefs.getBoolean(CloudBackupPrefsKeys.KEY_CHARGING_ONLY, false),
        cloudMeteredAllowed = prefs.getBoolean(CloudBackupPrefsKeys.KEY_METERED_ALLOWED, false),
        lastCloudBackupTime = prefs.getLong(CloudBackupPrefsKeys.KEY_LAST_TIME, 0L),
        lastCloudBackupFiles = prefs.getInt(CloudBackupPrefsKeys.KEY_LAST_FILES, 0),
        lastSyncStatus = try {
            SyncStatus.valueOf(prefs.getString(CloudBackupPrefsKeys.KEY_LAST_STATUS, "NONE") ?: "NONE")
        } catch (_: Exception) { SyncStatus.NONE },
        cloudFolderName = prefs.getString(CloudBackupPrefsKeys.KEY_FOLDER_NAME, "ElecPilot") ?: "ElecPilot"
    )
}

fun Context.saveCloudBackupPreferences(prefs: CloudBackupPreferences) {
    getSharedPreferences(CloudBackupPrefsKeys.PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
        putBoolean(CloudBackupPrefsKeys.KEY_ENABLED, prefs.cloudEnabled)
        putString(CloudBackupPrefsKeys.KEY_PROVIDER, prefs.cloudProvider.name)
        putString(CloudBackupPrefsKeys.KEY_FORMAT, prefs.cloudFormat.name)
        putInt(CloudBackupPrefsKeys.KEY_FREQUENCY, prefs.cloudFrequencyHours)
        putBoolean(CloudBackupPrefsKeys.KEY_WIFI_ONLY, prefs.cloudWifiOnly)
        putBoolean(CloudBackupPrefsKeys.KEY_CHARGING_ONLY, prefs.cloudChargingOnly)
        putBoolean(CloudBackupPrefsKeys.KEY_METERED_ALLOWED, prefs.cloudMeteredAllowed)
        putLong(CloudBackupPrefsKeys.KEY_LAST_TIME, prefs.lastCloudBackupTime)
        putInt(CloudBackupPrefsKeys.KEY_LAST_FILES, prefs.lastCloudBackupFiles)
        putString(CloudBackupPrefsKeys.KEY_LAST_STATUS, prefs.lastSyncStatus.name)
        putString(CloudBackupPrefsKeys.KEY_FOLDER_NAME, prefs.cloudFolderName)
        apply()
    }
}
