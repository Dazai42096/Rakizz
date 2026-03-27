package com.rakizz.student.data.network

import com.rakizz.student.data.remote.dto.AssignmentDto
import com.rakizz.student.data.remote.dto.AuthResponseDto
import com.rakizz.student.data.remote.dto.MaterialDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizGenerateRequestDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RakizzApi {

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponseDto

    @GET("api/v1/materials")
    suspend fun getMaterials(): List<MaterialDto>

    @GET("api/v1/materials/{id}")
    suspend fun getMaterial(
        @Path("id") id: String
    ): MaterialDto

    @GET("api/v1/assignments")
    suspend fun getAssignments(): List<AssignmentDto>

    @GET("api/v1/quizzes")
    suspend fun getQuizzes(): List<QuizDto>

    @GET("api/v1/quizzes/{id}")
    suspend fun getQuiz(
        @Path("id") id: String
    ): QuizDto

    @POST("api/v1/quizzes/generate")
    suspend fun generateQuiz(
        @Body request: QuizGenerateRequestDto
    ): QuizDto
}