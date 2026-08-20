package com.tab.utrabajo.api.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserView(
    val uid: String,
    val email: String,
    val role: String,
    val displayName: String,
    val photoUrl: String? = null,
)

data class AuthResponse(val token: String, val user: UserView)

data class RegisterStudentRequest(
    @field:Email val email: String,
    @field:Size(min = 8, max = 72) val password: String,
    @field:NotBlank @field:Size(max = 160) val fullName: String,
)

data class RegisterCompanyRequest(
    @field:NotBlank @field:Size(max = 40) val nit: String,
    @field:NotBlank @field:Size(max = 40) val phone: String,
    @field:Email val email: String,
    val workers: Int,
    @field:Size(min = 8, max = 72) val password: String,
)

data class LoginRequest(@field:Email val email: String, @field:NotBlank val password: String)

data class UpdateProfileRequest(
    @field:Size(max = 160) val displayName: String? = null,
    @field:Size(max = 40) val phone: String? = null,
    @field:Size(max = 240) val address: String? = null,
)

data class WorkInfoRequest(
    val worksNow: Boolean,
    @field:Size(max = 160) val companyName: String = "",
    @field:Size(max = 160) val role: String = "",
)

data class SkillsRequest(val skills: List<@Size(max = 100) String>)

data class JobRequest(
    @field:NotBlank @field:Size(max = 180) val title: String,
    @field:NotBlank val description: String,
    val requirements: List<@NotBlank @Size(max = 300) String>,
    @field:Size(max = 40) val salary: String? = null,
    @field:NotBlank @field:Size(max = 160) val location: String,
)

data class JobUpdateRequest(
    @field:Size(max = 180) val title: String? = null,
    @field:Size(max = 40) val salary: String? = null,
)

data class ApplicationRequest(val jobId: String)
data class ChatRequest(val jobId: String)
data class MessageRequest(@field:NotBlank @field:Size(max = 4000) val message: String)

data class IdentifierResponse(val id: String)
data class FileResponse(val url: String)
data class ApiError(val code: String, val message: String)
