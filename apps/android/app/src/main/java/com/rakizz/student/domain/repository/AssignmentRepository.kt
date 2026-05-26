package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.Assignment

interface AssignmentRepository {
    suspend fun getAssignments(): List<Assignment>

    suspend fun createAssignment(
        title: String,
        description: String,
        dueAt: String
    ): Assignment

    suspend fun updateAssignmentStatus(
        assignmentId: String,
        status: String
    ): Assignment

    suspend fun deleteAssignment(
        assignmentId: String
    )
}