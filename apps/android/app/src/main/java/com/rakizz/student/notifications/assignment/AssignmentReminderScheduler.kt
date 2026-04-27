package com.rakizz.student.notifications.assignment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object AssignmentReminderScheduler {

    const val CHANNEL_ID = "assignment_reminders"

    internal const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
    internal const val EXTRA_TITLE = "extra_title"
    internal const val EXTRA_DESCRIPTION = "extra_description"
    internal const val EXTRA_DUE_TEXT = "extra_due_text"

    private const val PREFS_NAME = "assignment_reminders"
    private const val KEY_RECORDS = "records"
    private const val REMINDER_OFFSET_MILLIS = 30L * 60L * 1000L

    fun scheduleReminder(
        context: Context,
        assignmentId: String,
        title: String,
        description: String,
        dueAtRaw: String
    ): String? {
        val appContext = context.applicationContext
        val dueInstant = parseDueInstant(dueAtRaw)
            ?: return "Local reminder was not scheduled because the due date format was invalid."

        val dueAtMillis = dueInstant.toEpochMilli()
        val now = System.currentTimeMillis()

        if (dueAtMillis <= now) {
            return "Local reminder was not scheduled because the due time is already in the past."
        }

        val triggerAtMillis = if (dueAtMillis - REMINDER_OFFSET_MILLIS > now) {
            dueAtMillis - REMINDER_OFFSET_MILLIS
        } else {
            dueAtMillis
        }

        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
            ?: return "Local reminder was not scheduled because AlarmManager is unavailable."

        val record = StoredReminder(
            assignmentId = assignmentId,
            title = title.ifBlank { "Assignment Reminder" },
            description = description,
            dueAtRaw = dueAtRaw,
            triggerAtMillis = triggerAtMillis
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            buildPendingIntent(appContext, record)
        )

        upsertRecord(appContext, record)
        return null
    }

    fun rescheduleAll(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        val now = System.currentTimeMillis()

        val stillValid = loadRecords(appContext)
            .filter { it.triggerAtMillis > now }

        stillValid.forEach { record ->
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                record.triggerAtMillis,
                buildPendingIntent(appContext, record)
            )
        }

        saveRecords(appContext, stillValid)
    }

    internal fun removeStoredReminder(
        context: Context,
        assignmentId: String
    ) {
        val updated = loadRecords(context.applicationContext)
            .filterNot { it.assignmentId == assignmentId }

        saveRecords(context.applicationContext, updated)
    }

    private fun buildPendingIntent(
        context: Context,
        record: StoredReminder
    ): PendingIntent {
        val intent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            putExtra(EXTRA_ASSIGNMENT_ID, record.assignmentId)
            putExtra(EXTRA_TITLE, record.title)
            putExtra(EXTRA_DESCRIPTION, record.description)
            putExtra(EXTRA_DUE_TEXT, formatDueText(record.dueAtRaw))
        }

        return PendingIntent.getBroadcast(
            context,
            record.assignmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatDueText(raw: String): String {
        val instant = parseDueInstant(raw) ?: return raw
        val formatter = DateTimeFormatter.ofPattern(
            "EEE, d MMM yyyy • h:mm a",
            Locale.getDefault()
        )
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }

    private fun parseDueInstant(raw: String): Instant? {
        val value = raw.trim()
        if (value.isBlank()) return null

        return try {
            Instant.parse(value)
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(value).toInstant()
            } catch (_: DateTimeParseException) {
                try {
                    ZonedDateTime.parse(value).toInstant()
                } catch (_: DateTimeParseException) {
                    try {
                        LocalDateTime.parse(value)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    } catch (_: DateTimeParseException) {
                        null
                    }
                }
            }
        }
    }

    private fun loadRecords(context: Context): List<StoredReminder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECORDS, "[]").orEmpty()

        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        StoredReminder(
                            assignmentId = item.optString("assignmentId"),
                            title = item.optString("title"),
                            description = item.optString("description"),
                            dueAtRaw = item.optString("dueAtRaw"),
                            triggerAtMillis = item.optLong("triggerAtMillis")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun upsertRecord(
        context: Context,
        record: StoredReminder
    ) {
        val existing = loadRecords(context)
            .filterNot { it.assignmentId == record.assignmentId } +
            record

        saveRecords(context, existing)
    }

    private fun saveRecords(
        context: Context,
        records: List<StoredReminder>
    ) {
        val array = JSONArray()

        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("assignmentId", record.assignmentId)
                    .put("title", record.title)
                    .put("description", record.description)
                    .put("dueAtRaw", record.dueAtRaw)
                    .put("triggerAtMillis", record.triggerAtMillis)
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECORDS, array.toString())
            .apply()
    }

    private data class StoredReminder(
        val assignmentId: String,
        val title: String,
        val description: String,
        val dueAtRaw: String,
        val triggerAtMillis: Long
    )
}