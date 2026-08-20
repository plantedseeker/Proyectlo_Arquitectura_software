package com.tab.utrabajo.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class DemoDataSeeder(
    private val jdbc: JdbcClient,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.seed-demo:true}") private val enabled: Boolean,
) : ApplicationRunner {
    @Transactional
    override fun run(args: ApplicationArguments) {
        if (!enabled) return
        val studentId = stableUuid("demo-student")
        val companyId = stableUuid("demo-company")
        insertUserIfMissing(
            studentId,
            "estudiante@utrabajo.local",
            "student",
            "Estudiante Demo",
        )
        insertUserIfMissing(
            companyId,
            "empresa@utrabajo.local",
            "company",
            "Empresa Demo SAS",
        )
        jdbc.sql(
            """
            INSERT INTO company_profile(user_id, nit, worker_count)
            VALUES (:id, '900123456-7', 25)
            ON CONFLICT (user_id) DO NOTHING
            """.trimIndent()
        ).param("id", companyId).update()

        val existingJobs = jdbc.sql("SELECT count(*) FROM job_offer WHERE company_id = :id")
            .param("id", companyId).query(Int::class.java).single()
        if (existingJobs == 0) {
            repeat(12) { index ->
                val jobId = stableUuid("demo-job-$index")
                jdbc.sql(
                    """
                    INSERT INTO job_offer(id, company_id, title, description, salary, location, active)
                    VALUES (:id, :companyId, :title, :description, :salary, :location, TRUE)
                    """.trimIndent()
                )
                    .param("id", jobId)
                    .param("companyId", companyId)
                    .param("title", listOf("Desarrollador Android", "Analista de datos", "Soporte TI")[index % 3])
                    .param("description", "Oferta sintética local número ${index + 1} para demostrar UTrabajo.")
                    .param("salary", BigDecimal(2_000_000 + index * 100_000))
                    .param("location", listOf("Bogotá", "Medellín", "Cali")[index % 3])
                    .update()
                listOf("Trabajo en equipo", "Comunicación", "Conocimientos técnicos").forEachIndexed { position, value ->
                    jdbc.sql(
                        "INSERT INTO job_requirement(job_id, position, requirement) VALUES (:id, :position, :value)"
                    ).param("id", jobId).param("position", position).param("value", value).update()
                }
            }
        }
    }

    private fun insertUserIfMissing(id: UUID, email: String, role: String, name: String) {
        jdbc.sql(
            """
            INSERT INTO app_user(id, email, password_hash, role, full_name, completed)
            VALUES (:id, :email, :password, :role, :name, TRUE)
            ON CONFLICT (email) DO NOTHING
            """.trimIndent()
        )
            .param("id", id)
            .param("email", email)
            .param("password", passwordEncoder.encode("UTrabajo1!"))
            .param("role", role)
            .param("name", name)
            .update()
    }

    private fun stableUuid(value: String): UUID =
        UUID.nameUUIDFromBytes(value.toByteArray(StandardCharsets.UTF_8))
}
