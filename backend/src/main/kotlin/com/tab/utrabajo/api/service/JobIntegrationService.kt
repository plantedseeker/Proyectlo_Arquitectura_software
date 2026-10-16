package com.tab.utrabajo.api.service

import com.tab.utrabajo.api.auth.NotFoundException
import com.tab.utrabajo.api.model.JobConversationContextResponse
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class JobIntegrationService(private val jdbc: JdbcClient) {
    fun conversationContext(jobId: UUID): JobConversationContextResponse = jdbc.sql(
        """
        SELECT id, company_id
        FROM job_offer
        WHERE id = :jobId
        """.trimIndent()
    )
        .param("jobId", jobId)
        .query { resultSet, _ ->
            JobConversationContextResponse(
                jobId = resultSet.getObject("id", UUID::class.java),
                companyId = resultSet.getObject("company_id", UUID::class.java),
            )
        }
        .list()
        .firstOrNull()
        ?: throw NotFoundException("Oferta no encontrada")
}
