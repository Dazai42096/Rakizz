package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.repository.AssignmentRepository
import javax.inject.Inject

class AssignmentRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : AssignmentRepository {
    override suspend fun getAssignments(): Result<List<Assignment>> {
        return try {
            val response = api.getAssignments()
            Result.success(response.map { 
                Assignment(it.id, it.title, it.due_date ?: "", it.status)
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
