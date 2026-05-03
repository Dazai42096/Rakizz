package com.rakizz.student.blocking

import android.content.Context
import com.rakizz.student.data.remote.dto.PolicyDto
import java.time.LocalTime
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object FocusRuleCache {

    private const val PREFS_NAME = "rakizz_focus_rules_cache"
    private const val KEY_RULES = "rules_text"
    private const val KEY_UNLOCK_PREFIX = "unlock_until_"

    fun savePolicies(
        context: Context,
        policies: List<PolicyDto>
    ) {
        val lines = mutableListOf<String>()

        policies.forEach { policy ->
            val config = policy.configJson

            val startTime = config["start_time"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val endTime = config["end_time"]?.jsonPrimitive?.contentOrNull.orEmpty()

            // new focus rule style: blocked_apps array
            val blockedApps = config["blocked_apps"]?.jsonArray

            if (blockedApps != null) {
                blockedApps.forEach { item ->
                    try {
                        val obj = item.jsonObject

                        val packageName = obj["package_name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                        val appName = obj["app_name"]?.jsonPrimitive?.contentOrNull.orEmpty()

                        if (packageName.isNotBlank()) {
                            lines.add(
                                listOf(
                                    clean(packageName),
                                    clean(appName),
                                    clean(startTime),
                                    clean(endTime)
                                ).joinToString("|")
                            )
                        }
                    } catch (_: Exception) {
                        // skip bad item
                    }
                }
            }

            // old fallback style, just in case
            val oldPackage = config["package_name"]?.jsonPrimitive?.contentOrNull.orEmpty()

            if (oldPackage.isNotBlank()) {
                lines.add(
                    listOf(
                        clean(oldPackage),
                        clean(oldPackage),
                        clean(startTime),
                        clean(endTime)
                    ).joinToString("|")
                )
            }
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RULES, lines.joinToString("\n"))
            .apply()
    }

    fun isPackageBlockedNow(
        context: Context,
        packageName: String
    ): Boolean {
        if (packageName.isBlank()) {
            return false
        }

        // if student already passed quiz, don't block for now
        if (isLocallyUnlocked(context, packageName)) {
            return false
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rulesText = prefs.getString(KEY_RULES, "").orEmpty()

        if (rulesText.isBlank()) {
            return false
        }

        val lines = rulesText.lines()

        for (line in lines) {
            val parts = line.split("|")

            if (parts.size < 4) {
                continue
            }

            val blockedPackage = parts[0]
            val startTime = parts[2]
            val endTime = parts[3]

            if (blockedPackage == packageName && isTimeActive(startTime, endTime)) {
                return true
            }
        }

        return false
    }

    fun saveLocalUnlock(
        context: Context,
        packageName: String,
        minutes: Int
    ) {
        val safeMinutes = if (minutes <= 0) 15 else minutes
        val until = System.currentTimeMillis() + (safeMinutes * 60_000L)

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_UNLOCK_PREFIX + packageName, until)
            .apply()
    }

    private fun isLocallyUnlocked(
        context: Context,
        packageName: String
    ): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val until = prefs.getLong(KEY_UNLOCK_PREFIX + packageName, 0L)

        return until > System.currentTimeMillis()
    }

    private fun isTimeActive(
        startText: String,
        endText: String
    ): Boolean {
        // if parent did not set time correctly, keep it active for preview
        if (startText.isBlank() || endText.isBlank()) {
            return true
        }

        return try {
            val now = LocalTime.now()
            val start = LocalTime.parse(startText)
            val end = LocalTime.parse(endText)

            if (start <= end) {
                now >= start && now <= end
            } else {
                // example: 22:00 to 02:00
                now >= start || now <= end
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun clean(value: String): String {
        return value
            .replace("|", "")
            .replace("\n", "")
            .trim()
    }
}