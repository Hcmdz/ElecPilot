package com.HcmDz.ElecPilot.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.HcmDz.ElecPilot.data.getCloudBackupPreferences
import com.HcmDz.ElecPilot.worker.CloudBackupWorker
import java.util.concurrent.TimeUnit

object CloudBackupScheduler {

    private const val WORK_NAME_PERIODIC = "cloud_backup_periodic"

    fun schedulePeriodic(context: Context, intervalHours: Int, wifiOnly: Boolean, meteredAllowed: Boolean, requiresCharging: Boolean = false) {
        require(intervalHours >= 1) { "Interval must be at least 1 hour" }

        val networkType = when {
            !meteredAllowed -> NetworkType.UNMETERED
            wifiOnly -> NetworkType.UNMETERED
            else -> NetworkType.CONNECTED
        }

        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .setRequiredNetworkType(networkType)
            .setRequiresCharging(requiresCharging)
            .build()

        val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
    }

    fun rescheduleIfNeeded(context: Context) {
        val prefs = context.getCloudBackupPreferences()
        if (prefs.cloudEnabled && prefs.cloudFrequencyHours > 0) {
            schedulePeriodic(context, prefs.cloudFrequencyHours, prefs.cloudWifiOnly, prefs.cloudMeteredAllowed, prefs.cloudChargingOnly)
        } else {
            cancelPeriodic(context)
        }
    }
}
