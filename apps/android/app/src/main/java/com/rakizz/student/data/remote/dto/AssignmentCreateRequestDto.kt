package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentCreateRequestDto(
    val title: String,
    val description: String? = null,
    @SerialName("due_at")
    val dueAt: String,
    @SerialName("student_id")
    val studentId: String? = null
)