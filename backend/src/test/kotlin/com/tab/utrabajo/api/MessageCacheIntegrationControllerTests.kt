package com.tab.utrabajo.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    properties = [
        "app.integration-token=test-integration-token",
        "app.message-cache.enabled=true",
        "app.message-cache.ttl-ms=1000",
        "app.message-cache.maximum-entries=1000",
    ]
)
@AutoConfigureMockMvc
class MessageCacheIntegrationControllerTests(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `valid integration token can inspect and reset cache statistics`() {
        mockMvc.perform(
            get("/internal/integration/v1/message-cache/stats")
                .header("X-Integration-Token", "test-integration-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.ttlMilliseconds").value(1000))

        mockMvc.perform(
            post("/internal/integration/v1/message-cache/reset")
                .header("X-Integration-Token", "test-integration-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.currentEntries").value(0))
            .andExpect(jsonPath("$.hits").value(0))
            .andExpect(jsonPath("$.misses").value(0))
    }

    @Test
    fun `missing or invalid integration token cannot inspect cache statistics`() {
        val path = "/internal/integration/v1/message-cache/stats"

        mockMvc.perform(get(path))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("unauthorized"))

        mockMvc.perform(get(path).header("X-Integration-Token", "invalid-token"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("unauthorized"))
    }
}
