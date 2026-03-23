package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String // Normally password shouldn't be here in plain text over DTO unless via HTTPS, but based on typical REST flows.
)

@Serializable
data class AuthResponseDto(
    val access_token: String,
    val token_type: String
)
