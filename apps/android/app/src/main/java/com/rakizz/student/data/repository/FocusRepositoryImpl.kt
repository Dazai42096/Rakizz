package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.PolicyDto
import com.rakizz.student.data.remote.dto.UsageEventDto
import com.rakizz.student.data.remote.dto.UsagePackageSummaryDto
import com.rakizz.student.data.remote.dto.UsageSummaryDto
import com.rakizz.student.data.remote.dto.UsageSyncRequestDto
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.UsagePackageSummary
import com.rakizz.student.domain.model.UsageSummary
import com.rakizz.student.domain.repository.FocusRepository
import javax.inject.Inject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class FocusRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : FocusRepository {

    override suspend fun getPolicies(): Result<List<FocusPolicy>> {
        return try {
            val policies = api.getPolicies().map { it.toDomain() }
            Result.success(policies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsageSummary(days: Int): Result<UsageSummary> {
        return try {
            val summary = api.getUsageSummary(days = days)
            Result.success(summary.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncDemoUsage(): Result<Int> {
        return try {
            // simple test data for the checkpoint demo
            val response = api.syncUsage(
                UsageSyncRequestDto(
                    events = listOf(
                        UsageEventDto(
                            packageName = "com.instagram.android",
                            durationSec = 600
                        ),
                        UsageEventDto(
                            packageName = "com.youtube.android",
                            durationSec = 420
                        )
                    )
                )
            )

            Result.success(response.savedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PolicyDto.toDomain(): FocusPolicy {
        return FocusPolicy(
            id = id,
            parentId = parentId,
            studentId = studentId,
            ruleType = ruleType,
            packageName = configJson.stringValue("package_name") ?: "unknown.app",
            dailyLimitMinutes = configJson.intValue("daily_limit_minutes"),
            note = configJson.stringValue("note")
        )
    }

    private fun UsageSummaryDto.toDomain(): UsageSummary {
        return UsageSummary(
            studentId = studentId,
            days = days,
            totalDurationSec = totalDurationSec,
            packages = packages.map { it.toDomain() }
        )
    }

    private fun UsagePackageSummaryDto.toDomain(): UsagePackageSummary {
        return UsagePackageSummary(
            packageName = packageName,
            durationSec = durationSec
        )
    }

    private fun kotlinx.serialization.json.JsonObject.stringValue(key: String): String? {
        return this[key]?.jsonPrimitive?.contentOrNull
    }

    private fun kotlinx.serialization.json.JsonObject.intValue(key: String): Int? {
        return this[key]?.jsonPrimitive?.intOrNull
    }
}