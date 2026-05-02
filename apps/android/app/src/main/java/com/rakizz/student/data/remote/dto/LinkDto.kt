package com.rakizz.student.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairCodeGenerateResponseDto(
    @SerialName("pair_code")
    val pairCode: String
)

@Serializable
data class PairCodeLinkRequestDto(
    @SerialName("pair_code")
    val pairCode: String
)

@Serializable
data class PairCodeLinkResponseDto(
    val status: String,
    val student: CurrentUserDto
)