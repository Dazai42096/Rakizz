package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizGenerateRequestDto(
    @SerialName("material_id")
    val materialId: String,
    val difficulty: String
)