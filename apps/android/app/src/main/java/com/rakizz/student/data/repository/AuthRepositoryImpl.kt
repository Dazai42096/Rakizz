package com.rakizz.student.data.repository

import com.rakizz.student.data.local.TokenManager
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.domain.model.AuthToken
import com.rakizz.student.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: RakizzApi,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun login(email: String, pass: String): Result<AuthToken> {
        return try {
            val response = api.login(email, pass)
            tokenManager.saveToken(response.access_token)
            Result.success(AuthToken(response.access_token, response.token_type))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
