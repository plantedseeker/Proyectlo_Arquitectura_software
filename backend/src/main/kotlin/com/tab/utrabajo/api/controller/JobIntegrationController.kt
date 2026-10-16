package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.auth.IntegrationTokenAuthenticator
import com.tab.utrabajo.api.model.JobConversationContextResponse
import com.tab.utrabajo.api.service.JobIntegrationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/integration/v1/jobs")
class JobIntegrationController(
    private val tokenAuthenticator: IntegrationTokenAuthenticator,
    private val service: JobIntegrationService,
) {
    @GetMapping("/{jobId}/conversation-context")
    fun conversationContext(
        @PathVariable jobId: UUID,
        @RequestHeader(name = "X-Integration-Token", required = false) integrationToken: String?,
    ): JobConversationContextResponse {
        tokenAuthenticator.requireValid(integrationToken)
        return service.conversationContext(jobId)
    }
}
