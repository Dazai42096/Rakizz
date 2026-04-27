package com.rakizz.student.domain.model

data class FocusPolicy(
    val id: String,
    val parentId: String,
    val studentId: String,
    val ruleType: String,
    val packageName: String,
    val dailyLimitMinutes: Int?,
    val note: String?
) {
    val title: String
        get() = when (ruleType.lowercase()) {
            "daily_limit" -> "Daily app limit"
            "time_window" -> "Study time block"
            "blocked_app" -> "Blocked app"
            else -> "Focus rule"
        }

    val limitText: String
        get() = dailyLimitMinutes?.let { "$it minutes/day" } ?: "No limit value"
}

data class UsagePackageSummary(
    val packageName: String,
    val durationSec: Int
) {
    val durationText: String
        get() {
            val minutes = durationSec / 60
            val seconds = durationSec % 60
            return if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
}

data class UsageSummary(
    val studentId: String,
    val days: Int,
    val totalDurationSec: Int,
    val packages: List<UsagePackageSummary>
) {
    val totalText: String
        get() {
            val minutes = totalDurationSec / 60
            val seconds = totalDurationSec % 60
            return if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
}