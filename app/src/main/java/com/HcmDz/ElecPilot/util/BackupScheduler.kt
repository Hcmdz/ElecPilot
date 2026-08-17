package com.HcmDz.ElecPilot.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.HcmDz.ElecPilot.data.getBackupPreferences
import com.HcmDz.ElecPilot.worker.BackupWorker
import java.util.concurrent.TimeUnit

object BackupScheduler {

    private const val WORK_NAME_PERIODIC = "auto_backup_periodic"
    private const val WORK_NAME_ONE_TIME = "auto_backup_one_time"

    fun schedulePeriodic(context: Context, intervalHours: Int) {
        require(intervalHours >= 1) { "Interval must be at least 1 hour" }
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<BackupWorker>(
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

    fun triggerOneTime(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun rescheduleIfNeeded(context: Context) {
        val prefs = context.getBackupPreferences()
        if (prefs.enabled && prefs.frequencyHours > 0) {
            schedulePeriodic(context, prefs.frequencyHours)
        } else {
            cancelPeriodic(context)
        }
    }
}
