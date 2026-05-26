package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload

interface MaterialRepository {
    suspend fun getMaterials(): Result<List<Material>>

    suspend fun getMaterial(id: String): Result<Material>

    suspend fun uploadMaterial(
        title: String?,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<Material>

    suspend fun createMaterialFromUrl(
        title: String,
        sourceUrl: String
    ): Result<Material>

    suspend fun downloadMaterial(id: String): Result<MaterialDownload>

    suspend fun deleteMaterial(id: String): Result<Unit>
}