package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.Quiz

interface QuizRepository {
    suspend fun getQuizzes(): Result<List<Quiz>>
    suspend fun getQuiz(id: String): Result<Quiz>
    suspend fun generateQuiz(materialId: String): Result<Quiz>
}
