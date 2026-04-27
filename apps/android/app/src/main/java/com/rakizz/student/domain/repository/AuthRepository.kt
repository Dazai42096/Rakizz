package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.AuthToken

interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<AuthToken>

    suspend fun register(
        fullName: String,
        email: String,
        pass: String
    ): Result<AuthToken>
}