package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.Material

interface MaterialRepository {
    suspend fun getMaterials(): Result<List<Material>>
    suspend fun getMaterial(id: String): Result<Material>
}
