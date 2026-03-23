package com.rakizz.student.data.repository

import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.repository.MaterialRepository
import javax.inject.Inject

class MaterialRepositoryImpl @Inject constructor(
    private val api: RakizzApi
) : MaterialRepository {
    override suspend fun getMaterials(): Result<List<Material>> {
        return try {
            val response = api.getMaterials()
            Result.success(response.map { 
                Material(it.id, it.title, it.description ?: "", it.url ?: "") 
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMaterial(id: String): Result<Material> {
        return try {
            val dto = api.getMaterial(id)
            Result.success(Material(dto.id, dto.title, dto.description ?: "", dto.url ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
