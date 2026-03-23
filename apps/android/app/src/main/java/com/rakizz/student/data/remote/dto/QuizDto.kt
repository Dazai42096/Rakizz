package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val description: String?,
    val score: Int?,
    val total_questions: Int
)

@Serializable
data class QuizGenerateRequestDto(
    val material_id: String
)
