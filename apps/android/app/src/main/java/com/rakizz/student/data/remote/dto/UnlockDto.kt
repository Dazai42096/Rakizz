package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnlockCheckRequestDto(
    @SerialName("package_name")
    val packageName: String,

    // true when accessibility service already detected the blocked app
    @SerialName("force_blocked")
    val forceBlocked: Boolean = false
)

@Serializable
data class UnlockCheckResponseDto(
    val blocked: Boolean,

    @SerialName("package_name")
    val packageName: String,

    @SerialName("app_name")
    val appName: String? = null,

    val message: String,

    @SerialName("material_id")
    val materialId: String? = null,

    @SerialName("unlocked_until")
    val unlockedUntil: String? = null
)

@Serializable
data class UnlockGrantRequestDto(
    @SerialName("package_name")
    val packageName: String,

    @SerialName("quiz_attempt_id")
    val quizAttemptId: String,

    @SerialName("granted_minutes")
    val grantedMinutes: Int = 15
)

@Serializable
data class UnlockGrantResponseDto(
    val status: String,

    @SerialName("package_name")
    val packageName: String,

    @SerialName("granted_minutes")
    val grantedMinutes: Int,

    @SerialName("expires_at")
    val expiresAt: String
)