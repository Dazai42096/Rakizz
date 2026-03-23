package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaterialDto(
    val id: String,
    val title: String,
    val description: String?,
    val url: String?
)
