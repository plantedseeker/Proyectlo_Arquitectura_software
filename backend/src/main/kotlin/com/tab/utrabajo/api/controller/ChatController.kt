package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.model.ChatRequest
import com.tab.utrabajo.api.model.IdentifierResponse
import com.tab.utrabajo.api.model.MessageRequest
import com.tab.utrabajo.api.service.UTrabajoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/chats")
class ChatController(private val service: UTrabajoService) {
    @GetMapping
    fun chats(authentication: Authentication): List<Map<String, Any?>> =
        service.chats(authentication.apiPrincipal())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(authentication: Authentication, @Valid @RequestBody request: ChatRequest) =
        IdentifierResponse(
            service.createOrGetChat(authentication.apiPrincipal(), UUID.fromString(request.jobId)).toString()
        )

    @GetMapping("/{chatId}/messages")
    fun messages(
        authentication: Authentication,
        @PathVariable chatId: UUID,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
    ): List<Map<String, Any?>> =
        service.messages(authentication.apiPrincipal(), chatId, limit, offset)

    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun send(
        authentication: Authentication,
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: MessageRequest,
    ) = IdentifierResponse(
        service.sendMessage(authentication.apiPrincipal(), chatId, request.message).toString()
    )
}
