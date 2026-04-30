package com.rakizz.student.data.network

import com.rakizz.student.data.remote.dto.AssignmentCreateRequestDto
import com.rakizz.student.data.remote.dto.AssignmentDto
import com.rakizz.student.data.remote.dto.AuthResponseDto
import com.rakizz.student.data.remote.dto.CurrentUserDto
import com.rakizz.student.data.remote.dto.InstalledAppsSyncRequestDto
import com.rakizz.student.data.remote.dto.InstalledAppsSyncResponseDto
import com.rakizz.student.data.remote.dto.MaterialDto
import com.rakizz.student.data.remote.dto.PolicyCreateRequestDto
import com.rakizz.student.data.remote.dto.PolicyDto
import com.rakizz.student.data.remote.dto.ProfileUpdateRequestDto
import com.rakizz.student.data.remote.dto.QuizAttemptRequestDto
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizGenerateRequestDto
import com.rakizz.student.data.remote.dto.RegisterRequestDto
import com.rakizz.student.data.remote.dto.StudentAppCatalogResponseDto
import com.rakizz.student.data.remote.dto.UsageSummaryDto
import com.rakizz.student.data.remote.dto.UsageSyncRequestDto
import com.rakizz.student.data.remote.dto.UsageSyncResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface RakizzApi {

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): AuthResponseDto

    @GET("api/v1/users/me")
    suspend fun getMe(): CurrentUserDto

    @PUT("api/v1/users/me")
    suspend fun updateMe(
        @Body request: ProfileUpdateRequestDto
    ): CurrentUserDto

    @GET("api/v1/materials")
    suspend fun getMaterials(): List<MaterialDto>

    @GET("api/v1/materials/{id}")
    suspend fun getMaterial(
        @Path("id") id: String
    ): MaterialDto

    @Multipart
    @POST("api/v1/materials")
    suspend fun uploadMaterial(
        @Part("title") title: RequestBody?,
        @Part file: MultipartBody.Part
    ): MaterialDto

    @Multipart
    @POST("api/v1/materials")
    suspend fun createMaterialFromUrl(
        @Part("title") title: RequestBody,
        @Part("source_url") sourceUrl: RequestBody
    ): MaterialDto

    @Streaming
    @GET("api/v1/materials/{id}/download")
    suspend fun downloadMaterial(
        @Path("id") id: String
    ): Response<ResponseBody>

    @GET("api/v1/assignments")
    suspend fun getAssignments(): List<AssignmentDto>

    @POST("api/v1/assignments")
    suspend fun createAssignment(
        @Body request: AssignmentCreateRequestDto
    ): AssignmentDto

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

    @POST("api/v1/quizzes/{id}/attempts")
    suspend fun submitQuizAttempt(
        @Path("id") id: String,
        @Body request: QuizAttemptRequestDto
    ): QuizAttemptResultDto

    @GET("api/v1/policies/")
    suspend fun getPolicies(): List<PolicyDto>

    @POST("api/v1/policies/")
    suspend fun createPolicy(
        @Body request: PolicyCreateRequestDto
    ): PolicyDto

    @POST("api/v1/usage/sync")
    suspend fun syncUsage(
        @Body request: UsageSyncRequestDto
    ): UsageSyncResponseDto

    @GET("api/v1/usage/summary")
    suspend fun getUsageSummary(
        @Query("days") days: Int = 7
    ): UsageSummaryDto

    @POST("api/v1/app-catalog/sync")
    suspend fun syncInstalledApps(
        @Body request: InstalledAppsSyncRequestDto
    ): InstalledAppsSyncResponseDto

    @GET("api/v1/app-catalog/student/{studentId}")
    suspend fun getStudentInstalledApps(
        @Path("studentId") studentId: String
    ): StudentAppCatalogResponseDto
}