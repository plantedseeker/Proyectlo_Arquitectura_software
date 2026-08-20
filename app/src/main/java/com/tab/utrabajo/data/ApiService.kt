package com.tab.utrabajo.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register/student") fun registerStudent(@Body request: RegisterStudentRequest): Call<AuthResponse>
    @POST("api/auth/register/company") fun registerCompany(@Body request: RegisterCompanyRequest): Call<AuthResponse>
    @POST("api/auth/login") fun login(@Body request: LoginRequest): Call<AuthResponse>
    @GET("api/auth/me") fun me(): Call<SessionUser>
    @DELETE("api/auth/session") fun logout(): Call<Unit>

    @GET("api/profile") fun profile(): Call<Map<String, Any?>>
    @PATCH("api/profile") fun updateProfile(@Body request: UpdateProfileRequest): Call<Unit>
    @PUT("api/profile/work-info") fun saveWorkInfo(@Body request: WorkInfoRequest): Call<Unit>
    @PUT("api/profile/skills") fun saveSkills(@Body request: SkillsRequest): Call<Unit>
    @Multipart @POST("api/profile/avatar") fun uploadAvatar(@Part file: MultipartBody.Part): Call<FileResponse>
    @Multipart @POST("api/profile/cv") fun uploadCv(@Part file: MultipartBody.Part): Call<FileResponse>

    @Multipart
    @POST("api/profile/company/representative")
    fun saveRepresentative(
        @Part("name") name: RequestBody,
        @Part("documentType") documentType: RequestBody,
        @Part("documentNumber") documentNumber: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Call<Unit>

    @Multipart
    @POST("api/profile/company/documents")
    fun uploadCompanyDocuments(
        @Part rut: MultipartBody.Part?,
        @Part chamber: MultipartBody.Part?,
    ): Call<Unit>

    @GET("api/jobs") fun activeJobs(): Call<List<Map<String, Any?>>>
    @POST("api/jobs") fun createJob(@Body request: JobRequest): Call<IdentifierResponse>
    @PATCH("api/jobs/{jobId}") fun updateJob(@Path("jobId") jobId: String, @Body request: JobUpdateRequest): Call<Unit>
    @DELETE("api/jobs/{jobId}") fun deleteJob(@Path("jobId") jobId: String): Call<Unit>

    @GET("api/applications") fun applications(): Call<List<Map<String, Any?>>>
    @POST("api/applications") fun apply(@Body request: ApplicationRequest): Call<IdentifierResponse>
    @DELETE("api/applications/{id}") fun cancelApplication(@Path("id") id: String): Call<Unit>

    @GET("api/chats") fun chats(): Call<List<Map<String, Any?>>>
    @POST("api/chats") fun createChat(@Body request: ChatRequest): Call<IdentifierResponse>
    @GET("api/chats/{chatId}/messages") fun messages(@Path("chatId") chatId: String): Call<List<Map<String, Any?>>>
    @POST("api/chats/{chatId}/messages") fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: MessageRequest,
    ): Call<IdentifierResponse>
}
