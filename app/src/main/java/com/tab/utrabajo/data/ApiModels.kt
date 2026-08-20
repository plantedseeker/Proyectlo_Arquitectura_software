package com.tab.utrabajo.data

data class SessionUser(
    val uid: String,
    val email: String,
    val role: String,
    val displayName: String,
    val photoUrl: String? = null,
)

data class AuthResponse(val token: String, val user: SessionUser)
data class RegisterStudentRequest(val email: String, val password: String, val fullName: String)
data class RegisterCompanyRequest(
    val nit: String,
    val phone: String,
    val email: String,
    val workers: Int,
    val password: String,
)
data class LoginRequest(val email: String, val password: String)
data class UpdateProfileRequest(val displayName: String? = null, val phone: String? = null, val address: String? = null)
data class WorkInfoRequest(val worksNow: Boolean, val companyName: String, val role: String)
data class SkillsRequest(val skills: List<String>)
data class JobRequest(
    val title: String,
    val description: String,
    val requirements: List<String>,
    val salary: String?,
    val location: String,
)
data class JobUpdateRequest(val title: String?, val salary: String?)
data class ApplicationRequest(val jobId: String)
data class ChatRequest(val jobId: String)
data class MessageRequest(val message: String)
data class IdentifierResponse(val id: String)
data class FileResponse(val url: String)
data class ApiError(val code: String? = null, val message: String? = null)
