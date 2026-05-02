package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentUserDto(
    val id: String,
    val email: String,
    val role: String,
    @SerialName("pair_code")
    val pairCode: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    val school: String? = null,
    @SerialName("grade_level")
    val gradeLevel: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null
)

@Serializable
data class ProfileUpdateRequestDto(
    @SerialName("full_name")
    val fullName: String? = null,
    val school: String? = null,
    @SerialName("grade_level")
    val gradeLevel: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null
)

@Serializable
data class InstalledAppDto(
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String,
    val category: String? = null
)

@Serializable
data class InstalledAppsSyncRequestDto(
    val apps: List<InstalledAppDto>
)

@Serializable
data class InstalledAppsSyncResponseDto(
    @SerialName("saved_count")
    val savedCount: Int
)

@Serializable
data class InstalledAppResponseDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String,
    val category: String? = null,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class StudentAppCatalogResponseDto(
    @SerialName("student_id")
    val studentId: String,
    val apps: List<InstalledAppResponseDto> = emptyList()
)