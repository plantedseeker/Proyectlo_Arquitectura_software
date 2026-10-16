package com.tab.utrabajo.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(properties = ["app.integration-token=test-integration-token"])
@AutoConfigureMockMvc
@Transactional
class JobIntegrationControllerTests(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `valid integration token returns the minimal conversation context`() {
        val jobId = createJob()

        mockMvc.perform(
            get("/internal/integration/v1/jobs/$jobId/conversation-context")
                .header("X-Integration-Token", "test-integration-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.jobId").value(jobId))
            .andExpect(jsonPath("$.companyId").isNotEmpty)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `missing or invalid integration token is rejected`() {
        val path = "/internal/integration/v1/jobs/${UUID.randomUUID()}/conversation-context"

        mockMvc.perform(get(path))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("unauthorized"))

        mockMvc.perform(get(path).header("X-Integration-Token", "invalid-token"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("unauthorized"))
    }

    @Test
    fun `unknown job returns not found with a valid integration token`() {
        mockMvc.perform(
            get("/internal/integration/v1/jobs/${UUID.randomUUID()}/conversation-context")
                .header("X-Integration-Token", "test-integration-token")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("not_found"))
    }

    private fun createJob(): String {
        val companyToken = registerCompany()
        val response = mockMvc.perform(
            post("/api/jobs")
                .header("Authorization", "Bearer $companyToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"title":"Desarrollador de integración","description":"Oferta del spike","requirements":["Kotlin"],"salary":"3000000","location":"Bogotá"}"""
                )
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return jsonValue(response, "id")
    }

    private fun registerCompany(): String {
        val response = mockMvc.perform(
            post("/api/auth/register/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"nit":"NIT-${UUID.randomUUID()}","phone":"3001234567","email":"company-${UUID.randomUUID()}@utrabajo.test","workers":25,"password":"UTrabajo1!"}"""
                )
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return jsonValue(response, "token")
    }

    private fun jsonValue(json: String, field: String): String =
        Regex("\\\"$field\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(json)?.groupValues?.get(1)
            ?: error("No se encontró '$field' en $json")
}
