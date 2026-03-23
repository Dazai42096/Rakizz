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
            Result.success(response.map { 
                Quiz(it.id, it.title, it.description ?: "", it.score, it.total_questions)
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuiz(id: String): Result<Quiz> {
        return try {
            val dto = api.getQuiz(id)
            Result.success(Quiz(dto.id, dto.title, dto.description ?: "", dto.score, dto.total_questions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateQuiz(materialId: String): Result<Quiz> {
        return try {
            val dto = api.generateQuiz(QuizGenerateRequestDto(materialId))
            Result.success(Quiz(dto.id, dto.title, dto.description ?: "", dto.score, dto.total_questions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
