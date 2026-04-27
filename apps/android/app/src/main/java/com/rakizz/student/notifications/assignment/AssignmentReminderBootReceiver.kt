package com.rakizz.student.notifications.assignment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AssignmentReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AssignmentReminderScheduler.rescheduleAll(context)
        }
    }
}