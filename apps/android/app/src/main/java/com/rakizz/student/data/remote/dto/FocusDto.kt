package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PolicyDto(
    val id: String,
    @SerialName("parent_id")
    val parentId: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("rule_type")
    val ruleType: String,
    @SerialName("config_json")
    val configJson: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class UsageEventDto(
    @SerialName("package_name")
    val packageName: String,
    @SerialName("duration_sec")
    val durationSec: Int
)

@Serializable
data class UsageSyncRequestDto(
    val events: List<UsageEventDto>
)

@Serializable
data class UsageSyncResponseDto(
    @SerialName("saved_count")
    val savedCount: Int
)

@Serializable
data class UsagePackageSummaryDto(
    @SerialName("package_name")
    val packageName: String,
    @SerialName("duration_sec")
    val durationSec: Int
)

@Serializable
data class UsageSummaryDto(
    @SerialName("student_id")
    val studentId: String,
    val days: Int,
    @SerialName("total_duration_sec")
    val totalDurationSec: Int,
    val packages: List<UsagePackageSummaryDto> = emptyList()
)