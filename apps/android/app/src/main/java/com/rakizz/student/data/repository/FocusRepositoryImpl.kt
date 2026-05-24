package com.rakizz.student.data.repository

import android.content.Context
import com.rakizz.student.blocking.FocusRuleCache
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.InstalledAppDto
import com.rakizz.student.data.remote.dto.InstalledAppsSyncRequestDto
import com.rakizz.student.data.remote.dto.PolicyDto
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.repository.FocusRepository
import com.rakizz.student.usage.InstalledAppsReader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FocusRepositoryImpl @Inject constructor(
    private val api: RakizzApi,
    private val installedAppsReader: InstalledAppsReader,
    @ApplicationContext private val context: Context
) : FocusRepository {

    override suspend fun getPolicies(): Result<List<FocusPolicy>> {
        return try {
            // Student loads parent rules from backend.
            val policies = api.getPolicies()

            // Important:
            // Accessibility service cannot call Compose/ViewModel directly.
            // It reads local cache, so we save the latest parent rules here.
            FocusRuleCache.savePolicies(
                context = context,
                policies = policies
            )

            Result.success(
                policies.map { policy ->
                    policy.toDomain()
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncInstalledApps(): Result<Int> {
        return try {
            // Student phone sends installed apps to backend.
            val phoneApps = installedAppsReader.readInstalledApps()

            val request = InstalledAppsSyncRequestDto(
                apps = phoneApps.map { app ->
                    InstalledAppDto(
                        packageName = app.packageName,
                        appName = app.appName,
                        category = null
                    )
                }
            )

            val response = api.syncInstalledApps(request)

            Result.success(response.savedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PolicyDto.toDomain(): FocusPolicy {
        val blockedApps = configJson["blocked_apps"]
            ?.jsonArray
            ?.mapNotNull { item ->
                try {
                    val obj = item.jsonObject

                    obj["app_name"]?.jsonPrimitive?.contentOrNull
                        ?: obj["package_name"]?.jsonPrimitive?.contentOrNull
                } catch (_: Exception) {
                    null
                }
            }
            .orEmpty()

        return FocusPolicy(
            id = id,
            parentId = parentId,
            studentId = studentId,
            ruleType = ruleType,
            packageName = configJson.stringValue("package_name")
                ?: blockedApps.firstOrNull()
                ?: "focus rule",
            dailyLimitMinutes = configJson.intValue("daily_limit_minutes"),
            note = configJson.stringValue("note"),
            startTime = configJson.stringValue("start_time"),
            endTime = configJson.stringValue("end_time"),
            blockedApps = blockedApps
        )
    }

    private fun JsonObject.stringValue(
        key: String
    ): String? {
        return this[key]?.jsonPrimitive?.contentOrNull
    }

    private fun JsonObject.intValue(
        key: String
    ): Int? {
        return this[key]?.jsonPrimitive?.intOrNull
    }
}