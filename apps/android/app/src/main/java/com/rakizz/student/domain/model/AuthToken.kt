package com.rakizz.student.domain.model

data class AuthToken(
    val accessToken: String,
    val tokenType: String
)
