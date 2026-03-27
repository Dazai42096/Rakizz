package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.QuizGenerateRequestDto
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.repository.QuizRepository
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : QuizRepository {

    override suspend fun getQuizzes(): Result<List<Quiz>> {
        return try {
            val response = api.getQuizzes()
            Result.success(
                response.map {
                    Quiz(
                        id = it.id,
                        title = it.title ?: "Quiz",
                        description = "",
                        score = it.score ?: 0,
                        totalQuestions = it.total_questions ?: 0
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuiz(id: String): Result<Quiz> {
        return try {
            val dto = api.getQuiz(id)
            Result.success(
                Quiz(
                    id = dto.id,
                    title = dto.title ?: "Quiz",
                    description = "",
                    score = dto.score ?: 0,
                    totalQuestions = dto.total_questions ?: 0
                )
            )
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
            Result.success(
                Quiz(
                    id = dto.id,
                    title = dto.title ?: "Quiz",
                    description = "",
                    score = dto.score ?: 0,
                    totalQuestions = dto.total_questions ?: 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}