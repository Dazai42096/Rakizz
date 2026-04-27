package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("due_at")
    val dueAt: String,
    val status: String = "PENDING",
    @SerialName("student_id")
    val studentId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("reminder_warning")
    val reminderWarning: String? = null
)