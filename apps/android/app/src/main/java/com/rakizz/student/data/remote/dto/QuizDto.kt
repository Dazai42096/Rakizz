package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String? = null,
    val score: Int? = null,
    val total_questions: Int? = null
)