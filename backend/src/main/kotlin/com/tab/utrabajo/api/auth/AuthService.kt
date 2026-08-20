package com.tab.utrabajo.api.auth

import com.tab.utrabajo.api.model.AuthResponse
import com.tab.utrabajo.api.model.RegisterCompanyRequest
import com.tab.utrabajo.api.model.RegisterStudentRequest
import com.tab.utrabajo.api.model.UserView
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

data class ApiPrincipal(val userId: UUID, val email: String, val role: String)

@Service
class AuthService(
    private val jdbc: JdbcClient,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.session-hours:168}") private val sessionHours: Long,
) {
    private val secureRandom = SecureRandom()

    @Transactional
    fun registerStudent(request: RegisterStudentRequest): AuthResponse {
        val userId = UUID.randomUUID()
        insertUser(
            userId = userId,
            email = request.email,
            password = request.password,
            role = "student",
            fullName = request.fullName,
            phone = null,
        )
        return createSession(loadUser(userId))
    }

    @Transactional
    fun registerCompany(request: RegisterCompanyRequest): AuthResponse {
        require(request.workers >= 0) { "El número de trabajadores no puede ser negativo" }
        val userId = UUID.randomUUID()
        insertUser(
            userId = userId,
            email = request.email,
            password = request.password,
            role = "company",
            fullName = "Empresa ${request.nit}",
            phone = request.phone,
        )
        try {
            jdbc.sql("INSERT INTO company_profile(user_id, nit, worker_count) VALUES (:id, :nit, :workers)")
                .param("id", userId)
                .param("nit", request.nit.trim())
                .param("workers", request.workers)
                .update()
        } catch (error: DuplicateKeyException) {
            throw ConflictException("Ya existe una empresa con ese NIT")
        }
        return createSession(loadUser(userId))
    }

    fun login(email: String, password: String): AuthResponse {
        val row = jdbc.sql(
            """
            SELECT id, email, password_hash, role, full_name, photo_path
            FROM app_user WHERE lower(email) = lower(:email)
            """.trimIndent()
        ).param("email", email.trim()).query { rs, _ ->
            LoginRow(
                id = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                passwordHash = rs.getString("password_hash"),
                role = rs.getString("role"),
                fullName = rs.getString("full_name"),
                photoPath = rs.getString("photo_path"),
            )
        }.list().firstOrNull() ?: throw UnauthorizedException("Correo o contraseña incorrectos")

        if (!passwordEncoder.matches(password, row.passwordHash)) {
            throw UnauthorizedException("Correo o contraseña incorrectos")
        }
        return createSession(row.toUserView())
    }

    fun findPrincipal(rawToken: String): ApiPrincipal? {
        val hash = tokenHash(rawToken)
        return jdbc.sql(
            """
            SELECT u.id, u.email, u.role
            FROM auth_session s
            JOIN app_user u ON u.id = s.user_id
            WHERE s.token_hash = :hash AND s.expires_at > CURRENT_TIMESTAMP
            """.trimIndent()
        ).param("hash", hash).query { rs, _ ->
            ApiPrincipal(
                userId = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                role = rs.getString("role"),
            )
        }.list().firstOrNull()
    }

    fun logout(rawToken: String) {
        jdbc.sql("DELETE FROM auth_session WHERE token_hash = :hash")
            .param("hash", tokenHash(rawToken))
            .update()
    }

    fun currentUser(userId: UUID): UserView = loadUser(userId)

    private fun insertUser(
        userId: UUID,
        email: String,
        password: String,
        role: String,
        fullName: String,
        phone: String?,
    ) {
        try {
            jdbc.sql(
                """
                INSERT INTO app_user(id, email, password_hash, role, full_name, phone)
                VALUES (:id, lower(:email), :passwordHash, :role, :fullName, :phone)
                """.trimIndent()
            )
                .param("id", userId)
                .param("email", email.trim())
                .param("passwordHash", passwordEncoder.encode(password))
                .param("role", role)
                .param("fullName", fullName.trim())
                .param("phone", phone)
                .update()
        } catch (error: DuplicateKeyException) {
            throw ConflictException("Ya existe una cuenta con ese correo")
        }
    }

    private fun createSession(user: UserView): AuthResponse {
        val bytes = ByteArray(32).also(secureRandom::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        jdbc.sql(
            """
            INSERT INTO auth_session(token_hash, user_id, expires_at)
            VALUES (:hash, :userId, :expiresAt)
            """.trimIndent()
        )
            .param("hash", tokenHash(token))
            .param("userId", UUID.fromString(user.uid))
            .param("expiresAt", Timestamp.from(Instant.now().plus(sessionHours, ChronoUnit.HOURS)))
            .update()
        return AuthResponse(token, user)
    }

    private fun loadUser(userId: UUID): UserView = jdbc.sql(
        "SELECT id, email, role, full_name, photo_path FROM app_user WHERE id = :id"
    ).param("id", userId).query { rs, _ ->
        UserView(
            uid = rs.getObject("id", UUID::class.java).toString(),
            email = rs.getString("email"),
            role = rs.getString("role"),
            displayName = rs.getString("full_name"),
            photoUrl = rs.getString("photo_path"),
        )
    }.single()

    private fun tokenHash(token: String): String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private data class LoginRow(
        val id: UUID,
        val email: String,
        val passwordHash: String,
        val role: String,
        val fullName: String,
        val photoPath: String?,
    ) {
        fun toUserView() = UserView(id.toString(), email, role, fullName, photoPath)
    }
}

class UnauthorizedException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
