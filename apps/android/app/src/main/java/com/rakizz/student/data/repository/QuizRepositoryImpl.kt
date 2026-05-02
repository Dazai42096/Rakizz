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
import javax.inject.Inject
import kotlin.math.roundToInt

class QuizRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : QuizRepository {

    override suspend fun getQuizzes(): Result<List<Quiz>> {
        return try {
            val response = api.getQuizzes()

            Result.success(
                response.map { dto ->
                    dto.toDomainSummary()
                }
            )
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
            // student does not choose the difficulty anymore
            // we always ask backend for a mixed AI quiz
            val dto = api.generateQuiz(
                QuizGenerateRequestDto(
                    materialId = materialId,
                    difficulty = "MIXED"
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
                request = QuizAttemptRequestDto(
                    answers = answers
                )
            )

            Result.success(dto.toResultQuiz())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun QuizDto.toDomainSummary(): Quiz {
        val count = totalQuestions ?: questions.size

        return Quiz(
            id = id,
            title = title ?: titleFromCount(count),
            description = "Mixed AI quiz generated from uploaded material.",
            score = score,
            totalQuestions = count,
            materialId = materialId ?: "",
            questions = emptyList(),
            passed = score?.let { it >= 70 },
            reviewData = emptyList()
        )
    }

    private fun QuizDto.toDomainDetailed(): Quiz {
        val count = totalQuestions ?: questions.size

        return Quiz(
            id = id,
            title = title ?: titleFromCount(count),
            description = "Answer the mixed AI questions and submit to get your score.",
            score = score,
            totalQuestions = count,
            materialId = materialId ?: "",
            questions = questions.map { question ->
                question.toDomain()
            },
            passed = score?.let { it >= 70 },
            reviewData = emptyList()
        )
    }

    private fun QuizAttemptResultDto.toResultQuiz(): Quiz {
        return Quiz(
            id = quizSetId,
            title = "Quiz Result",
            description = "Your answers were graded by the backend.",
            score = (score * 100.0).roundToInt(),
            totalQuestions = reviewData.size,
            materialId = "",
            questions = emptyList(),
            passed = passed,
            reviewData = reviewData.map { item ->
                item.toDomain()
            }
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

    private fun titleFromCount(count: Int): String {
        return if (count >= 20) {
            "Mixed AI Quiz"
        } else {
            "AI Quiz"
        }
    }
}