package com.HcmDz.ElecPilot.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.db.AppDatabase
import com.HcmDz.ElecPilot.data.db.PlcDatabase
import com.HcmDz.ElecPilot.data.SyncStatus
import com.HcmDz.ElecPilot.data.getCloudBackupPreferences
import com.HcmDz.ElecPilot.data.saveCloudBackupPreferences
import com.HcmDz.ElecPilot.util.CloudBackupManager
import com.HcmDz.ElecPilot.util.CloudBackupResult
import com.HcmDz.ElecPilot.util.NotificationHelper
import com.HcmDz.ElecPilot.util.RcloneDriveService
import kotlinx.coroutines.CancellationException

class CloudBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            RcloneDriveService.init(applicationContext)
            val prefs = applicationContext.getCloudBackupPreferences()
            if (!prefs.cloudEnabled) {
                return Result.success()
            }

            val signedIn = RcloneDriveService.isSignedIn(applicationContext)
            if (!signedIn) {
                return Result.failure()
            }

            val db = AppDatabase.getInstance(applicationContext)
            val plcDb = PlcDatabase.getInstance(applicationContext)

            val motors = db.motorDao().getAllMotorsOnce()
            val plcs = plcDb.plcDao().getAllPlcOnce()

            if (motors.isEmpty() && plcs.isEmpty()) {
                return Result.success()
            }

            val startPrefs = applicationContext.getCloudBackupPreferences()
            applicationContext.saveCloudBackupPreferences(
                startPrefs.copy(lastSyncStatus = SyncStatus.IN_PROGRESS)
            )

            val result = CloudBackupManager.performCloudBackup(
                context = applicationContext,
                motors = motors,
                plcList = plcs,
                format = prefs.cloudFormat,
                folderName = prefs.cloudFolderName
            )

            when (result) {
                is CloudBackupResult.Success -> {
                    val currentPrefs = applicationContext.getCloudBackupPreferences()
                    applicationContext.saveCloudBackupPreferences(
                        currentPrefs.copy(
                            lastCloudBackupTime = System.currentTimeMillis(),
                            lastCloudBackupFiles = result.fileCount,
                            lastSyncStatus = SyncStatus.SUCCESS
                        )
                    )
                    NotificationHelper.showBackupSuccess(applicationContext, result.motorCount, result.plcCount)
                    Result.success()
                }
                is CloudBackupResult.Error -> {
                    android.util.Log.w("CloudBackupWorker", "Backup failed")
                    NotificationHelper.showBackupError(applicationContext)
                    val currentPrefs = applicationContext.getCloudBackupPreferences()
                    applicationContext.saveCloudBackupPreferences(
                        currentPrefs.copy(
                            lastSyncStatus = SyncStatus.FAILED
                        )
                    )
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("CloudBackupWorker", "Worker failed", e)
            if (runAttemptCount < 3) Result.retry()
            else {
                NotificationHelper.showBackupError(applicationContext)
                Result.failure()
            }
        }
    }
}
