package com.tab.utrabajo.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class UTrabajoApiApplicationTests(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `registration jobs applications and chat use PostgreSQL end to end`() {
        val unique = UUID.randomUUID().toString()
        val companyToken = registerCompany("company-$unique@utrabajo.test")

        val jobResponse = mockMvc.perform(
            post("/api/jobs")
                .header("Authorization", "Bearer $companyToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"title":"Desarrollador Kotlin","description":"Oferta de prueba integrada","requirements":["Kotlin","SQL"],"salary":"3000000","location":"Bogotá"}"""
                )
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val jobId = jsonValue(jobResponse, "id")

        mockMvc.perform(get("/api/jobs?limit=100").header("Authorization", "Bearer $companyToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").exists())

        val studentToken = registerStudent("student-$unique@utrabajo.test")
        val applicationResponse = mockMvc.perform(
            post("/api/applications")
                .header("Authorization", "Bearer $studentToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobId":"$jobId"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val applicationId = jsonValue(applicationResponse, "id")

        mockMvc.perform(get("/api/applications").header("Authorization", "Bearer $studentToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].jobId").value(jobId))

        val chatResponse = mockMvc.perform(
            post("/api/chats")
                .header("Authorization", "Bearer $studentToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobId":"$jobId"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val chatId = jsonValue(chatResponse, "id")

        mockMvc.perform(
            post("/api/chats/$chatId/messages")
                .header("Authorization", "Bearer $studentToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"Hola, me interesa la oferta"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/api/chats/$chatId/messages").header("Authorization", "Bearer $companyToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].message").value("Hola, me interesa la oferta"))

        mockMvc.perform(delete("/api/applications/$applicationId").header("Authorization", "Bearer $studentToken"))
            .andExpect(status().isNoContent)
    }

    private fun registerCompany(email: String): String {
        val response = mockMvc.perform(
            post("/api/auth/register/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nit":"NIT-${UUID.randomUUID()}","phone":"3001234567","email":"$email","workers":25,"password":"UTrabajo1!"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return jsonValue(response, "token")
    }

    private fun registerStudent(email: String): String {
        val response = mockMvc.perform(
            post("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"UTrabajo1!","fullName":"Estudiante de prueba"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return jsonValue(response, "token")
    }

    private fun jsonValue(json: String, field: String): String =
        Regex("\\\"$field\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(json)?.groupValues?.get(1)
            ?: error("No se encontró '$field' en $json")
}
