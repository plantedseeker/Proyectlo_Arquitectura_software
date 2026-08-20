package com.tab.utrabajo.api.service

import com.tab.utrabajo.api.auth.ApiPrincipal
import com.tab.utrabajo.api.auth.ConflictException
import com.tab.utrabajo.api.auth.ForbiddenException
import com.tab.utrabajo.api.auth.NotFoundException
import com.tab.utrabajo.api.model.JobRequest
import com.tab.utrabajo.api.model.JobUpdateRequest
import com.tab.utrabajo.api.model.UpdateProfileRequest
import com.tab.utrabajo.api.model.WorkInfoRequest
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Service
class UTrabajoService(private val jdbc: JdbcClient) {
    fun profile(userId: UUID): Map<String, Any?> {
        val profile = jdbc.sql(
            """
            SELECT u.id, u.email, u.role, u.full_name, u.phone, u.address, u.photo_path, u.cv_path,
                   u.works_now, u.current_company, u.current_job_role, u.completed,
                   c.nit, c.worker_count, c.representative_name, c.document_type, c.document_number,
                   c.representative_document_path, c.rut_path, c.chamber_of_commerce_path
            FROM app_user u
            LEFT JOIN company_profile c ON c.user_id = u.id
            WHERE u.id = :id
            """.trimIndent()
        ).param("id", userId).query { rs, _ ->
            linkedMapOf<String, Any?>(
                "uid" to rs.getObject("id", UUID::class.java).toString(),
                "email" to rs.getString("email"),
                "rol" to rs.getString("role"),
                "nombre" to rs.getString("full_name"),
                "telefono" to rs.getString("phone"),
                "direccion" to rs.getString("address"),
                "photoUrl" to rs.getString("photo_path"),
                "cvUrl" to rs.getString("cv_path"),
                "trabajaActual" to rs.getObject("works_now"),
                "empresaActual" to rs.getString("current_company"),
                "rolActual" to rs.getString("current_job_role"),
                "completado" to rs.getBoolean("completed"),
                "nit" to rs.getString("nit"),
                "numeroTrabajadores" to rs.getObject("worker_count"),
                "representanteLegal" to rs.getString("representative_name"),
                "tipoDocumento" to rs.getString("document_type"),
                "numeroDocumento" to rs.getString("document_number"),
                "documentoRepresentanteUrl" to rs.getString("representative_document_path"),
                "rutUrl" to rs.getString("rut_path"),
                "camaraComercioUrl" to rs.getString("chamber_of_commerce_path"),
            )
        }.single()

        val skills = jdbc.sql("SELECT skill FROM student_skill WHERE user_id = :id ORDER BY skill")
            .param("id", userId)
            .query(String::class.java)
            .list()
        return profile + ("habilidades" to skills)
    }

    fun updateProfile(userId: UUID, request: UpdateProfileRequest) {
        jdbc.sql(
            """
            UPDATE app_user
            SET full_name = COALESCE(:displayName, full_name),
                phone = COALESCE(:phone, phone),
                address = COALESCE(:address, address),
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            """.trimIndent()
        )
            .param("displayName", request.displayName?.trim()?.takeIf(String::isNotEmpty))
            .param("phone", request.phone?.trim())
            .param("address", request.address?.trim())
            .param("id", userId)
            .update()
    }

    fun saveWorkInfo(userId: UUID, request: WorkInfoRequest) {
        jdbc.sql(
            """
            UPDATE app_user
            SET works_now = :worksNow, current_company = :company, current_job_role = :role,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :id AND role = 'student'
            """.trimIndent()
        )
            .param("worksNow", request.worksNow)
            .param("company", request.companyName.trim().takeIf { request.worksNow })
            .param("role", request.role.trim().takeIf { request.worksNow })
            .param("id", userId)
            .update()
    }

    @Transactional
    fun saveSkills(userId: UUID, skills: List<String>) {
        jdbc.sql("DELETE FROM student_skill WHERE user_id = :id").param("id", userId).update()
        skills.map(String::trim)
            .filter(String::isNotEmpty)
            .distinctBy(String::lowercase)
            .forEach { skill ->
                jdbc.sql("INSERT INTO student_skill(user_id, skill) VALUES (:id, :skill)")
                    .param("id", userId)
                    .param("skill", skill)
                    .update()
            }
    }

    fun updateStoredFile(userId: UUID, column: String, url: String) {
        require(column in setOf("photo_path", "cv_path")) { "Columna de archivo inválida" }
        jdbc.sql("UPDATE app_user SET $column = :url, completed = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
            .param("url", url)
            .param("id", userId)
            .update()
    }

    fun saveRepresentative(
        userId: UUID,
        name: String,
        documentType: String,
        documentNumber: String,
        documentUrl: String?,
    ) {
        val updated = jdbc.sql(
            """
            UPDATE company_profile
            SET representative_name = :name, document_type = :type, document_number = :number,
                representative_document_path = COALESCE(:url, representative_document_path)
            WHERE user_id = :id
            """.trimIndent()
        )
            .param("name", name.trim())
            .param("type", documentType.trim())
            .param("number", documentNumber.trim())
            .param("url", documentUrl)
            .param("id", userId)
            .update()
        if (updated == 0) throw ForbiddenException("Solo una empresa puede registrar representante")
    }

    fun saveCompanyDocuments(userId: UUID, rutUrl: String?, chamberUrl: String?) {
        val updated = jdbc.sql(
            """
            UPDATE company_profile
            SET rut_path = COALESCE(:rut, rut_path),
                chamber_of_commerce_path = COALESCE(:chamber, chamber_of_commerce_path)
            WHERE user_id = :id
            """.trimIndent()
        )
            .param("rut", rutUrl)
            .param("chamber", chamberUrl)
            .param("id", userId)
            .update()
        if (updated == 0) throw ForbiddenException("Solo una empresa puede registrar documentos")
        jdbc.sql("UPDATE app_user SET completed = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
            .param("id", userId)
            .update()
    }

    fun activeJobs(limit: Int = 100, offset: Int = 0): List<Map<String, Any?>> {
        require(limit in 1..200) { "limit debe estar entre 1 y 200" }
        require(offset >= 0) { "offset no puede ser negativo" }
        val jobs = jdbc.sql(
            """
            SELECT j.id, j.company_id, j.title, j.description, j.salary, j.location,
                   j.active, j.published_at
            FROM job_offer j
            WHERE j.active = TRUE
            ORDER BY j.published_at DESC, j.id
            LIMIT :limit OFFSET :offset
            """.trimIndent()
        ).param("limit", limit).param("offset", offset).query { rs, _ ->
            JobRow(
                id = rs.getObject("id", UUID::class.java),
                companyId = rs.getObject("company_id", UUID::class.java),
                title = rs.getString("title"),
                description = rs.getString("description"),
                salary = rs.getBigDecimal("salary"),
                location = rs.getString("location"),
                active = rs.getBoolean("active"),
                publishedAt = rs.getTimestamp("published_at").toInstant(),
            )
        }.list()
        val requirements = requirementsByJob(jobs.map(JobRow::id))
        return jobs.map { it.toMap(requirements[it.id].orEmpty()) }
    }

    @Transactional
    fun createJob(principal: ApiPrincipal, request: JobRequest): UUID {
        requireRole(principal, "company")
        val jobId = UUID.randomUUID()
        jdbc.sql(
            """
            INSERT INTO job_offer(id, company_id, title, description, salary, location)
            VALUES (:id, :companyId, :title, :description, :salary, :location)
            """.trimIndent()
        )
            .param("id", jobId)
            .param("companyId", principal.userId)
            .param("title", request.title.trim())
            .param("description", request.description.trim())
            .param("salary", parseSalary(request.salary))
            .param("location", request.location.trim())
            .update()
        request.requirements.map(String::trim).filter(String::isNotEmpty).distinctBy(String::lowercase)
            .forEachIndexed { index, requirement ->
                jdbc.sql(
                    "INSERT INTO job_requirement(job_id, position, requirement) VALUES (:jobId, :position, :requirement)"
                )
                    .param("jobId", jobId)
                    .param("position", index)
                    .param("requirement", requirement)
                    .update()
            }
        return jobId
    }

    fun updateJob(principal: ApiPrincipal, jobId: UUID, request: JobUpdateRequest) {
        requireRole(principal, "company")
        val count = jdbc.sql(
            """
            UPDATE job_offer
            SET title = COALESCE(:title, title), salary = COALESCE(:salary, salary),
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :id AND company_id = :companyId
            """.trimIndent()
        )
            .param("title", request.title?.trim()?.takeIf(String::isNotEmpty))
            .param("salary", request.salary?.let(::parseSalary))
            .param("id", jobId)
            .param("companyId", principal.userId)
            .update()
        if (count == 0) throw NotFoundException("Oferta no encontrada")
    }

    fun deleteJob(principal: ApiPrincipal, jobId: UUID) {
        requireRole(principal, "company")
        val count = jdbc.sql("DELETE FROM job_offer WHERE id = :id AND company_id = :companyId")
            .param("id", jobId)
            .param("companyId", principal.userId)
            .update()
        if (count == 0) throw NotFoundException("Oferta no encontrada")
    }

    fun applications(principal: ApiPrincipal): List<Map<String, Any?>> {
        requireRole(principal, "student")
        return jdbc.sql(
            """
            SELECT a.id, a.job_id, a.student_id, j.company_id, j.title, a.status, a.applied_at
            FROM application a JOIN job_offer j ON j.id = a.job_id
            WHERE a.student_id = :studentId AND a.status = 'active'
            ORDER BY a.applied_at DESC
            """.trimIndent()
        ).param("studentId", principal.userId).query { rs, _ ->
            linkedMapOf(
                "id" to rs.getObject("id", UUID::class.java).toString(),
                "jobId" to rs.getObject("job_id", UUID::class.java).toString(),
                "studentId" to rs.getObject("student_id", UUID::class.java).toString(),
                "companyId" to rs.getObject("company_id", UUID::class.java).toString(),
                "jobTitle" to rs.getString("title"),
                "status" to rs.getString("status"),
                "applicationDate" to rs.getTimestamp("applied_at").toInstant().toString(),
            )
        }.list()
    }

    fun apply(principal: ApiPrincipal, jobId: UUID): UUID {
        requireRole(principal, "student")
        val active = jdbc.sql("SELECT count(*) FROM job_offer WHERE id = :id AND active = TRUE")
            .param("id", jobId).query(Int::class.java).single()
        if (active == 0) throw NotFoundException("La oferta no está disponible")
        val id = UUID.randomUUID()
        try {
            jdbc.sql("INSERT INTO application(id, job_id, student_id) VALUES (:id, :jobId, :studentId)")
                .param("id", id)
                .param("jobId", jobId)
                .param("studentId", principal.userId)
                .update()
        } catch (error: DuplicateKeyException) {
            throw ConflictException("Ya te postulaste a esta oferta")
        }
        return id
    }

    fun cancelApplication(principal: ApiPrincipal, applicationId: UUID) {
        requireRole(principal, "student")
        val count = jdbc.sql(
            "UPDATE application SET status = 'cancelled' WHERE id = :id AND student_id = :studentId"
        )
            .param("id", applicationId)
            .param("studentId", principal.userId)
            .update()
        if (count == 0) throw NotFoundException("Postulación no encontrada")
    }

    fun createOrGetChat(principal: ApiPrincipal, jobId: UUID): UUID {
        requireRole(principal, "student")
        val companyId = jdbc.sql("SELECT company_id FROM job_offer WHERE id = :id")
            .param("id", jobId)
            .query(UUID::class.java)
            .list().firstOrNull() ?: throw NotFoundException("Oferta no encontrada")
        return jdbc.sql(
            """
            INSERT INTO chat(id, student_id, company_id, job_id)
            VALUES (:id, :studentId, :companyId, :jobId)
            ON CONFLICT (student_id, job_id) DO UPDATE SET job_id = EXCLUDED.job_id
            RETURNING id
            """.trimIndent()
        )
            .param("id", UUID.randomUUID())
            .param("studentId", principal.userId)
            .param("companyId", companyId)
            .param("jobId", jobId)
            .query(UUID::class.java)
            .single()
    }

    fun chats(principal: ApiPrincipal): List<Map<String, Any?>> {
        val field = if (principal.role == "student") "c.student_id" else "c.company_id"
        return jdbc.sql(
            """
            SELECT c.id, c.student_id, c.company_id, c.job_id, j.title,
                   c.created_at, c.last_message, c.last_message_at,
                   student.full_name AS student_name, company.full_name AS company_name
            FROM chat c
            JOIN job_offer j ON j.id = c.job_id
            JOIN app_user student ON student.id = c.student_id
            JOIN app_user company ON company.id = c.company_id
            WHERE $field = :userId
            ORDER BY c.last_message_at DESC
            """.trimIndent()
        ).param("userId", principal.userId).query { rs, _ ->
            linkedMapOf(
                "id" to rs.getObject("id", UUID::class.java).toString(),
                "studentId" to rs.getObject("student_id", UUID::class.java).toString(),
                "companyId" to rs.getObject("company_id", UUID::class.java).toString(),
                "jobId" to rs.getObject("job_id", UUID::class.java).toString(),
                "jobTitle" to rs.getString("title"),
                "createdAt" to rs.getTimestamp("created_at").toInstant().toString(),
                "lastMessage" to rs.getString("last_message"),
                "lastMessageTime" to rs.getTimestamp("last_message_at").toInstant().toString(),
                "studentName" to rs.getString("student_name"),
                "companyName" to rs.getString("company_name"),
            )
        }.list()
    }

    fun messages(principal: ApiPrincipal, chatId: UUID): List<Map<String, Any?>> {
        requireChatParticipant(principal.userId, chatId)
        return jdbc.sql(
            "SELECT id, chat_id, sender_id, body, sent_at FROM message WHERE chat_id = :chatId ORDER BY sent_at"
        ).param("chatId", chatId).query { rs, _ ->
            linkedMapOf(
                "id" to rs.getObject("id", UUID::class.java).toString(),
                "chatId" to rs.getObject("chat_id", UUID::class.java).toString(),
                "senderId" to rs.getObject("sender_id", UUID::class.java).toString(),
                "message" to rs.getString("body"),
                "timestamp" to rs.getTimestamp("sent_at").toInstant().toString(),
            )
        }.list()
    }

    @Transactional
    fun sendMessage(principal: ApiPrincipal, chatId: UUID, body: String): UUID {
        requireChatParticipant(principal.userId, chatId)
        val message = body.trim()
        require(message.isNotEmpty()) { "El mensaje está vacío" }
        val id = UUID.randomUUID()
        jdbc.sql("INSERT INTO message(id, chat_id, sender_id, body) VALUES (:id, :chatId, :senderId, :body)")
            .param("id", id)
            .param("chatId", chatId)
            .param("senderId", principal.userId)
            .param("body", message)
            .update()
        jdbc.sql(
            "UPDATE chat SET last_message = :body, last_message_at = CURRENT_TIMESTAMP WHERE id = :chatId"
        ).param("body", message).param("chatId", chatId).update()
        return id
    }

    private fun requireChatParticipant(userId: UUID, chatId: UUID) {
        val count = jdbc.sql(
            "SELECT count(*) FROM chat WHERE id = :chatId AND (student_id = :userId OR company_id = :userId)"
        ).param("chatId", chatId).param("userId", userId).query(Int::class.java).single()
        if (count == 0) throw ForbiddenException("No perteneces a este chat")
    }

    private fun requirementsByJob(jobIds: List<UUID>): Map<UUID, List<String>> {
        if (jobIds.isEmpty()) return emptyMap()
        return jdbc.sql(
            "SELECT job_id, requirement FROM job_requirement WHERE job_id IN (:ids) ORDER BY job_id, position"
        ).param("ids", jobIds).query { rs, _ ->
            rs.getObject("job_id", UUID::class.java) to rs.getString("requirement")
        }.list().groupBy({ it.first }, { it.second })
    }

    private fun requireRole(principal: ApiPrincipal, role: String) {
        if (principal.role != role) throw ForbiddenException("Esta acción requiere el rol $role")
    }

    private fun parseSalary(value: String?): BigDecimal? {
        val digits = value?.filter(Char::isDigit).orEmpty()
        return digits.takeIf(String::isNotEmpty)?.toBigDecimal()
    }

    private data class JobRow(
        val id: UUID,
        val companyId: UUID,
        val title: String,
        val description: String,
        val salary: BigDecimal?,
        val location: String,
        val active: Boolean,
        val publishedAt: Instant,
    ) {
        fun toMap(requirements: List<String>): Map<String, Any?> = linkedMapOf(
            "id" to id.toString(),
            "empresaId" to companyId.toString(),
            "titulo" to title,
            "descripcion" to description,
            "requisitos" to requirements,
            "salario" to salary?.stripTrailingZeros()?.toPlainString(),
            "ubicacion" to location,
            "activa" to active,
            "fechaPublicacion" to publishedAt.toString(),
        )
    }
}
