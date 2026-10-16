package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.auth.IntegrationTokenAuthenticator
import com.tab.utrabajo.api.service.MessagePageCache
import com.tab.utrabajo.api.service.MessagePageCacheStats
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/integration/v1/message-cache")
class MessageCacheIntegrationController(
    private val tokenAuthenticator: IntegrationTokenAuthenticator,
    private val cache: MessagePageCache,
) {
    @GetMapping("/stats")
    fun stats(
        @RequestHeader(name = "X-Integration-Token", required = false) integrationToken: String?,
    ): MessagePageCacheStats {
        tokenAuthenticator.requireValid(integrationToken)
        return cache.stats()
    }

    @PostMapping("/reset")
    fun reset(
        @RequestHeader(name = "X-Integration-Token", required = false) integrationToken: String?,
    ): MessagePageCacheStats {
        tokenAuthenticator.requireValid(integrationToken)
        cache.reset()
        return cache.stats()
    }
}
