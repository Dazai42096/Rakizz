package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestionDto(
    val id: String,
    @SerialName("question_text")
    val questionText: String,
    val options: List<String> = emptyList()
)

@Serializable
data class QuizReviewItemDto(
    val id: String,
    @SerialName("question_text")
    val questionText: String,
    val options: List<String> = emptyList(),
    @SerialName("correct_answer")
    val correctAnswer: String,
    val explanation: String,
    @SerialName("source_chunk_snippet")
    val sourceChunkSnippet: String? = null
)

@Serializable
data class QuizDto(
    val id: String,
    val title: String? = null,
    val score: Int? = null,
    @SerialName("total_questions")
    val totalQuestions: Int? = null,
    @SerialName("material_id")
    val materialId: String? = null,
    val questions: List<QuizQuestionDto> = emptyList()
)

@Serializable
data class QuizAttemptRequestDto(
    val answers: Map<String, String>
)

@Serializable
data class QuizAttemptResultDto(
    val id: String,
    @SerialName("quiz_set_id")
    val quizSetId: String,
    @SerialName("student_id")
    val studentId: String,
    val score: Double,
    val passed: Boolean,
    @SerialName("review_data")
    val reviewData: List<QuizReviewItemDto> = emptyList()
)