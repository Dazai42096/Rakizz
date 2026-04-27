package com.rakizz.student.notifications.assignment

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class AssignmentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val assignmentId = intent.getStringExtra(AssignmentReminderScheduler.EXTRA_ASSIGNMENT_ID).orEmpty()
        val title = intent.getStringExtra(AssignmentReminderScheduler.EXTRA_TITLE)
            .orEmpty()
            .ifBlank { "Assignment Reminder" }
        val description = intent.getStringExtra(AssignmentReminderScheduler.EXTRA_DESCRIPTION).orEmpty()
        val dueText = intent.getStringExtra(AssignmentReminderScheduler.EXTRA_DUE_TEXT).orEmpty()

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AssignmentReminderScheduler.removeStoredReminder(appContext, assignmentId)
            return
        }

        createNotificationChannel(appContext)

        val launchIntent = appContext.packageManager
            .getLaunchIntentForPackage(appContext.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(appContext.packageName)
            }

        val contentPendingIntent = PendingIntent.getActivity(
            appContext,
            assignmentId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("Due ")
            append(dueText)
            if (description.isNotBlank()) {
                append(" • ")
                append(description)
            }
        }

        val notification = Notification.Builder(
            appContext,
            AssignmentReminderScheduler.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(Notification.BigTextStyle().bigText(contentText))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        appContext.getSystemService(NotificationManager::class.java)
            ?.notify(assignmentId.hashCode(), notification)

        AssignmentReminderScheduler.removeStoredReminder(appContext, assignmentId)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            AssignmentReminderScheduler.CHANNEL_ID,
            "Assignment Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminder notifications for upcoming assignments"
        }

        manager.createNotificationChannel(channel)
    }
}