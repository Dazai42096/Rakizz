package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentDto(
    val id: String,
    val title: String,
    val due_date: String?,
    val status: String
)
