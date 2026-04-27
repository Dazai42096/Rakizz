package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMaterialRequestDto(
    val title: String,
    val description: String? = null,
    @SerialName("source_url")
    val sourceUrl: String
)