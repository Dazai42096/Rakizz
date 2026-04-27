package com.rakizz.student.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val access_token: String,
    val token_type: String
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val role: String
)