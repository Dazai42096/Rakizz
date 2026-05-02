package com.rakizz.student.domain.model

data class FocusPolicy(
    val id: String,
    val parentId: String,
    val studentId: String,
    val ruleType: String,
    val packageName: String,
    val dailyLimitMinutes: Int?,
    val note: String?,
    val startTime: String? = null,
    val endTime: String? = null,
    val blockedApps: List<String> = emptyList()
) {
    val title: String
        get() = when (ruleType.lowercase()) {
            "time_window" -> "Focus time rule"
            "daily_limit" -> "Daily app limit"
            "blocked_app" -> "Blocked app"
            else -> "Focus rule"
        }

    val timeText: String
        get() {
            if (startTime != null && endTime != null) {
                return "$startTime - $endTime"
            }

            return dailyLimitMinutes?.let {
                "$it minutes/day"
            } ?: "No time set"
        }

    val appsText: String
        get() {
            return if (blockedApps.isNotEmpty()) {
                blockedApps.joinToString(", ")
            } else {
                packageName
            }
        }
}