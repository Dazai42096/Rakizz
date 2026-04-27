package com.rakizz.student.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val dueAtRaw: String,
    val status: String,
    val reminderWarning: String? = null
) {
    val dueAtDisplay: String
        get() = formatAssignmentDueAt(dueAtRaw)

    val isCompleted: Boolean
        get() = status.equals("COMPLETED", ignoreCase = true)
}

fun parseAssignmentSortEpoch(raw: String): Long {
    return parseAssignmentInstant(raw)?.toEpochMilli() ?: Long.MAX_VALUE
}

private fun formatAssignmentDueAt(raw: String): String {
    val instant = parseAssignmentInstant(raw) ?: return raw
    val formatter = DateTimeFormatter.ofPattern(
        "EEE, d MMM yyyy • h:mm a",
        Locale.getDefault()
    )
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

private fun parseAssignmentInstant(raw: String): Instant? {
    val value = raw.trim()
    if (value.isBlank()) return null

    try {
        return Instant.parse(value)
    } catch (_: DateTimeParseException) {
    }

    try {
        return OffsetDateTime.parse(value).toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return ZonedDateTime.parse(value).toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return LocalDateTime.parse(value)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return LocalDate.parse(value)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    } catch (_: DateTimeParseException) {
    }

    return null
}