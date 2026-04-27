package com.rakizz.student.domain.model

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>
)

data class QuizReviewItem(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val sourceChunkSnippet: String?
)

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val score: Int?,
    val totalQuestions: Int,
    val materialId: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val passed: Boolean? = null,
    val reviewData: List<QuizReviewItem> = emptyList()
)