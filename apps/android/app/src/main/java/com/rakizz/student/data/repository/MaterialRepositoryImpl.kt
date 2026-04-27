package com.rakizz.student.data.repository

import android.content.Context
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.domain.repository.MaterialRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class MaterialRepositoryImpl @Inject constructor(
    private val api: RakizzApi,
    @ApplicationContext private val context: Context
) : MaterialRepository {

    override suspend fun getMaterials(): Result<List<Material>> {
        return try {
            val response = api.getMaterials()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMaterial(id: String): Result<Material> {
        return try {
            val dto = api.getMaterial(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadMaterial(
        title: String?,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<Material> {
        return try {
            val safeMimeType = mimeType.ifBlank { "application/octet-stream" }
            val titlePart = title
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.toRequestBody("text/plain".toMediaType())

            val fileBody = bytes.toRequestBody(safeMimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = fileBody
            )

            val dto = api.uploadMaterial(
                title = titlePart,
                file = filePart
            )

            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createMaterialFromUrl(
        title: String,
        sourceUrl: String
    ): Result<Material> {
        return try {
            val dto = api.createMaterialFromUrl(
                title = title.trim().toRequestBody("text/plain".toMediaType()),
                sourceUrl = sourceUrl.trim().toRequestBody("text/plain".toMediaType())
            )

            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadMaterial(id: String): Result<MaterialDownload> {
        return try {
            val response = api.downloadMaterial(id)

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to download material. HTTP ${response.code()}")
                )
            }

            val body = response.body()
                ?: return Result.failure(Exception("Downloaded file was empty"))

            val contentType = response.headers()["Content-Type"]
                ?.substringBefore(";")
                ?.trim()
                .orEmpty()
                .ifBlank { "application/octet-stream" }

            val rawFileName = parseFileName(response.headers()["Content-Disposition"])
                ?: "material_$id"

            val safeFileName = sanitizeFileName(rawFileName)
            val materialsDir = File(context.filesDir, "materials").apply {
                mkdirs()
            }
            val localFile = File(materialsDir, safeFileName)

            body.byteStream().use { input ->
                localFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(
                MaterialDownload(
                    filePath = localFile.absolutePath,
                    mimeType = contentType,
                    displayName = safeFileName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseFileName(contentDisposition: String?): String? {
        if (contentDisposition.isNullOrBlank()) return null

        val utf8Match = Regex("""filename\*=UTF-8''([^;]+)""").find(contentDisposition)
        if (utf8Match != null) {
            return URLDecoder.decode(
                utf8Match.groupValues[1],
                StandardCharsets.UTF_8.name()
            )
        }

        val simpleMatch = Regex("""filename="?([^"]+)"?""").find(contentDisposition)
        return simpleMatch?.groupValues?.get(1)
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("""[\\/:*?"<>|]"""), "_")
    }

    private fun com.rakizz.student.data.remote.dto.MaterialDto.toDomain(): Material {
        return Material(
            id = id,
            title = title,
            description = description ?: "",
            url = url ?: ""
        )
    }
}