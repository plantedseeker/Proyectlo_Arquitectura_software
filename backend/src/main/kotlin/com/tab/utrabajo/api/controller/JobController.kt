package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.model.IdentifierResponse
import com.tab.utrabajo.api.model.JobRequest
import com.tab.utrabajo.api.model.JobUpdateRequest
import com.tab.utrabajo.api.service.UTrabajoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/jobs")
class JobController(private val service: UTrabajoService) {
    @GetMapping
    fun activeJobs(
        @RequestParam(defaultValue = "100") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
    ): List<Map<String, Any?>> = service.activeJobs(limit, offset)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(authentication: Authentication, @Valid @RequestBody request: JobRequest) =
        IdentifierResponse(service.createJob(authentication.apiPrincipal(), request).toString())

    @PatchMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(
        authentication: Authentication,
        @PathVariable jobId: UUID,
        @Valid @RequestBody request: JobUpdateRequest,
    ) = service.updateJob(authentication.apiPrincipal(), jobId, request)

    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(authentication: Authentication, @PathVariable jobId: UUID) =
        service.deleteJob(authentication.apiPrincipal(), jobId)
}
