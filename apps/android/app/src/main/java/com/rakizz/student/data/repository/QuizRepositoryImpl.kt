package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.QuizAttemptRequestDto
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizGenerateRequestDto
import com.rakizz.student.data.remote.dto.QuizQuestionDto
import com.rakizz.student.data.remote.dto.QuizReviewItemDto
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.model.QuizQuestion
import com.rakizz.student.domain.model.QuizReviewItem
import com.rakizz.student.domain.repository.QuizRepository
import kotlin.math.roundToInt
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : QuizRepository {

    override suspend fun getQuizzes(): Result<List<Quiz>> {
        return try {
            val response = api.getQuizzes()
            Result.success(response.map { it.toDomainSummary() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuiz(id: String): Result<Quiz> {
        return try {
            val dto = api.getQuiz(id)
            Result.success(dto.toDomainDetailed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateQuiz(materialId: String): Result<Quiz> {
        return try {
            val dto = api.generateQuiz(
                QuizGenerateRequestDto(
                    materialId = materialId,
                    difficulty = "EASY"
                )
            )
            Result.success(dto.toDomainDetailed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitQuizAttempt(
        quizId: String,
        answers: Map<String, String>
    ): Result<Quiz> {
        return try {
            val dto = api.submitQuizAttempt(
                id = quizId,
                request = QuizAttemptRequestDto(answers = answers)
            )
            Result.success(dto.toResultQuiz())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun QuizDto.toDomainSummary(): Quiz {
        return Quiz(
            id = id,
            title = title ?: "Quiz",
            description = "",
            score = score,
            totalQuestions = totalQuestions ?: questions.size,
            materialId = materialId ?: "",
            questions = emptyList(),
            passed = score?.let { it >= 70 },
            reviewData = emptyList()
        )
    }

    private fun QuizDto.toDomainDetailed(): Quiz {
        return Quiz(
            id = id,
            title = title ?: "Generated Quiz",
            description = "Solve the questions below and submit to get your real score.",
            score = score,
            totalQuestions = totalQuestions ?: questions.size,
            materialId = materialId ?: "",
            questions = questions.map { it.toDomain() },
            passed = score?.let { it >= 70 },
            reviewData = emptyList()
        )
    }

    private fun QuizAttemptResultDto.toResultQuiz(): Quiz {
        return Quiz(
            id = quizSetId,
            title = "Quiz Result",
            description = "Your answers were submitted and graded successfully.",
            score = (score * 100.0).roundToInt(),
            totalQuestions = reviewData.size,
            materialId = "",
            questions = emptyList(),
            passed = passed,
            reviewData = reviewData.map { it.toDomain() }
        )
    }

    private fun QuizQuestionDto.toDomain(): QuizQuestion {
        return QuizQuestion(
            id = id,
            questionText = questionText,
            options = options
        )
    }

    private fun QuizReviewItemDto.toDomain(): QuizReviewItem {
        return QuizReviewItem(
            id = id,
            questionText = questionText,
            options = options,
            correctAnswer = correctAnswer,
            explanation = explanation,
            sourceChunkSnippet = sourceChunkSnippet
        )
    }
}