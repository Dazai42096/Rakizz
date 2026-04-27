package com.rakizz.student.data.repository

import com.rakizz.student.data.local.TokenManager
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.RegisterRequestDto
import com.rakizz.student.domain.model.AuthToken
import com.rakizz.student.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
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
            Result.failure(Exception(readableError(e)))
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        pass: String
    ): Result<AuthToken> {
        return try {
            val cleanEmail = email.trim().lowercase()

            val response = api.register(
                RegisterRequestDto(
                    email = cleanEmail,
                    password = pass,
                    role = "student"
                )
            )

            tokenManager.saveToken(response.access_token)
            Result.success(AuthToken(response.access_token, response.token_type))
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    private fun readableError(error: Exception): String {
        return when (error) {
            is HttpException -> {
                val body = try {
                    error.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                body ?: "HTTP ${error.code()}"
            }
            is IOException -> {
                "Network error. Check backend connection."
            }
            else -> {
                error.message ?: "Unknown error"
            }
        }
    }
}