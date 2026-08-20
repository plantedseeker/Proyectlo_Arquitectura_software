package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.model.ApplicationRequest
import com.tab.utrabajo.api.model.IdentifierResponse
import com.tab.utrabajo.api.service.UTrabajoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/applications")
class ApplicationController(private val service: UTrabajoService) {
    @GetMapping
    fun list(authentication: Authentication): List<Map<String, Any?>> =
        service.applications(authentication.apiPrincipal())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun apply(authentication: Authentication, @Valid @RequestBody request: ApplicationRequest) =
        IdentifierResponse(
            service.apply(authentication.apiPrincipal(), UUID.fromString(request.jobId)).toString()
        )

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(authentication: Authentication, @PathVariable applicationId: UUID) =
        service.cancelApplication(authentication.apiPrincipal(), applicationId)
}
