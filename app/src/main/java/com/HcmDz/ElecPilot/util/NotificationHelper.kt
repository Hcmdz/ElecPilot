package com.HcmDz.ElecPilot.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.HcmDz.ElecPilot.MainActivity
import com.HcmDz.ElecPilot.R

object NotificationHelper {

    private const val CHANNEL_ID = "cloud_backup_channel"
    private const val NOTIF_ID_SUCCESS = 1001
    private const val NOTIF_ID_ERROR = 1002
    private const val NOTIF_ID_DELETED = 1003
    const val NOTIF_ID_PROGRESS = 1004
    private const val NOTIF_ID_PROGRESS_FALLBACK = 1005

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notif_channel_desc)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun createAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showBackupSuccess(context: Context, motorCount: Int, plcCount: Int) {
        if (!hasPermission(context)) return
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notif_backup_success_title))
            .setContentText(context.getString(R.string.notif_backup_success_body, motorCount, plcCount))
            .setContentIntent(createAppIntent(context))
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID_SUCCESS, notification)
    }

    fun showBackupError(context: Context) {
        if (!hasPermission(context)) return
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notif_backup_error_title))
            .setContentText(context.getString(R.string.notif_backup_error_body_generic))
            .setContentIntent(createAppIntent(context))
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID_ERROR, notification)
    }

    fun showBackupDeleted(context: Context, fileName: String) {
        if (!hasPermission(context)) return
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .setContentTitle(context.getString(R.string.notif_backup_deleted_title))
            .setContentText(context.getString(R.string.notif_backup_deleted_body, fileName))
            .setContentIntent(createAppIntent(context))
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID_DELETED, notification)
    }

    fun buildProgressNotification(context: Context, title: String, text: String, percent: Int): android.app.Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(createAppIntent(context))
            .setProgress(100, percent, false)
            .setOngoing(true)
            .build()
    }

    fun updateProgressNotification(context: Context, percent: Int, speedKBs: Long) {
        if (!hasPermission(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = buildProgressNotification(
            context,
            context.getString(R.string.notif_backup_progress_title),
            context.getString(R.string.notif_backup_progress_body, percent, speedKBs),
            percent
        )
        nm.notify(NOTIF_ID_PROGRESS, notification)
    }

    fun cancelProgressNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID_PROGRESS)
    }
}
