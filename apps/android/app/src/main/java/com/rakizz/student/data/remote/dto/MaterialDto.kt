package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterialDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("source_url")
    val url: String? = null
)