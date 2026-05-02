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
            // get rules parent made for this student
            val policies = api.getPolicies()

            // save rules locally so accessibility service can block apps
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
            // student phone sends its apps to backend
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
        val blockedAppNames = mutableListOf<String>()
        val blockedPackages = mutableListOf<String>()

        val blockedAppsArray = configJson["blocked_apps"]?.jsonArray

        blockedAppsArray?.forEach { item ->
            try {
                val obj = item.jsonObject

                val packageName = obj["package_name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val appName = obj["app_name"]?.jsonPrimitive?.contentOrNull.orEmpty()

                if (packageName.isNotBlank()) {
                    blockedPackages.add(packageName)
                }

                if (appName.isNotBlank()) {
                    blockedAppNames.add(appName)
                }
            } catch (_: Exception) {
                // skip bad app item
            }
        }

        val firstPackage = configJson.stringValue("package_name")
            ?: blockedPackages.firstOrNull()
            ?: blockedAppNames.firstOrNull()
            ?: "focus_rule"

        val namesForUi = if (blockedAppNames.isNotEmpty()) {
            blockedAppNames
        } else {
            blockedPackages
        }

        return FocusPolicy(
            id = id,
            parentId = parentId,
            studentId = studentId,
            ruleType = ruleType,
            packageName = firstPackage,
            dailyLimitMinutes = configJson.intValue("daily_limit_minutes"),
            note = configJson.stringValue("note"),
            startTime = configJson.stringValue("start_time"),
            endTime = configJson.stringValue("end_time"),
            blockedApps = namesForUi
        )
    }

    private fun kotlinx.serialization.json.JsonObject.stringValue(key: String): String? {
        return this[key]?.jsonPrimitive?.contentOrNull
    }

    private fun kotlinx.serialization.json.JsonObject.intValue(key: String): Int? {
        return this[key]?.jsonPrimitive?.intOrNull
    }
}