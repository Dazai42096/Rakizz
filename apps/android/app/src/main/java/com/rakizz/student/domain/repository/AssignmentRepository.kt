package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.Assignment

interface AssignmentRepository {
    suspend fun getAssignments(): Result<List<Assignment>>
}
