package com.HcmDz.ElecPilot.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.HcmDz.ElecPilot.data.BackupModules
import com.HcmDz.ElecPilot.data.db.AppDatabase
import com.HcmDz.ElecPilot.data.db.PlcDatabase
import com.HcmDz.ElecPilot.data.getBackupPreferences
import com.HcmDz.ElecPilot.util.BackupManager
import com.HcmDz.ElecPilot.util.BackupResult
import kotlinx.coroutines.CancellationException

class BackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getBackupPreferences()
            if (!prefs.enabled) return Result.success()

            val db = AppDatabase.getInstance(applicationContext)
            val plcDb = PlcDatabase.getInstance(applicationContext)

            val motors = when (prefs.modules) {
                BackupModules.PLC -> emptyList()
                else -> db.motorDao().getAllMotorsOnce()
            }
            val plcs = when (prefs.modules) {
                BackupModules.DEPARTS -> emptyList()
                else -> plcDb.plcDao().getAllPlcOnce()
            }

            if (motors.isEmpty() && plcs.isEmpty()) {
                return Result.success()
            }

            val result = BackupManager.performBackup(applicationContext, motors, plcs)

            when (result) {
                is BackupResult.Success -> Result.success()
                is BackupResult.Error -> {
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
