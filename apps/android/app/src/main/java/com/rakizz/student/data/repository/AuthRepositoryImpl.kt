package com.rakizz.student.data.repository

import com.rakizz.student.data.local.TokenManager
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.RegisterRequestDto
import com.rakizz.student.domain.model.AuthToken
import com.rakizz.student.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val api: RakizzApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(
        email: String,
        pass: String
    ): Result<AuthToken> {
        return try {
            val cleanEmail = email.trim().lowercase()

            // email is used as the username in backend login
            val response = api.login(
                username = cleanEmail,
                password = pass
            )

            // save token so the app can use protected endpoints
            tokenManager.saveToken(response.access_token)

            Result.success(
                AuthToken(
                    accessToken = response.access_token,
                    tokenType = response.token_type
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(getSimpleError(e)))
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        pass: String,
        role: String
    ): Result<AuthToken> {
        return try {
            val cleanEmail = email.trim().lowercase()

            // fullName is only for the screen now
            // backend currently saves email, password and role
            val response = api.register(
                RegisterRequestDto(
                    email = cleanEmail,
                    password = pass,
                    role = role
                )
            )

            // after signup we keep the token directly
            tokenManager.saveToken(response.access_token)

            Result.success(
                AuthToken(
                    accessToken = response.access_token,
                    tokenType = response.token_type
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(getSimpleError(e)))
        }
    }

    private fun getSimpleError(error: Exception): String {
        return when (error) {
            is HttpException -> {
                val body = try {
                    error.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                body ?: "HTTP error ${error.code()}"
            }

            is IOException -> {
                "Network error. Check backend connection."
            }

            else -> {
                error.message ?: "Something went wrong"
            }
        }
    }
}