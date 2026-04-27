package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.AssignmentCreateRequestDto
import com.rakizz.student.data.remote.dto.AssignmentDto
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.model.parseAssignmentSortEpoch
import com.rakizz.student.domain.repository.AssignmentRepository
import javax.inject.Inject

class AssignmentRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : AssignmentRepository {

    override suspend fun getAssignments(): List<Assignment> {
        return api.getAssignments()
            .map { it.toDomain() }
            .sortedBy { parseAssignmentSortEpoch(it.dueAtRaw) }
    }

    override suspend fun createAssignment(
        title: String,
        description: String,
        dueAt: String
    ): Assignment {
        val created = api.createAssignment(
            AssignmentCreateRequestDto(
                title = title.trim(),
                description = description.trim().ifBlank { null },
                dueAt = dueAt,
                studentId = null
            )
        )
        return created.toDomain()
    }

    private fun AssignmentDto.toDomain(): Assignment {
        return Assignment(
            id = id,
            title = title,
            description = description.orEmpty(),
            dueAtRaw = dueAt,
            status = status.ifBlank { "PENDING" },
            reminderWarning = reminderWarning
        )
    }
}