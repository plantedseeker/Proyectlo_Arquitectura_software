package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.model.FileResponse
import com.tab.utrabajo.api.model.SkillsRequest
import com.tab.utrabajo.api.model.UpdateProfileRequest
import com.tab.utrabajo.api.model.WorkInfoRequest
import com.tab.utrabajo.api.service.FileStorageService
import com.tab.utrabajo.api.service.UTrabajoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val service: UTrabajoService,
    private val storage: FileStorageService,
) {
    @GetMapping
    fun profile(authentication: Authentication): Map<String, Any?> =
        service.profile(authentication.apiPrincipal().userId)

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest,
    ) = service.updateProfile(authentication.apiPrincipal().userId, request)

    @PutMapping("/work-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun workInfo(authentication: Authentication, @Valid @RequestBody request: WorkInfoRequest) =
        service.saveWorkInfo(authentication.apiPrincipal().userId, request)

    @PutMapping("/skills")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun skills(authentication: Authentication, @Valid @RequestBody request: SkillsRequest) =
        service.saveSkills(authentication.apiPrincipal().userId, request.skills)

    @PostMapping("/avatar", consumes = ["multipart/form-data"])
    fun avatar(authentication: Authentication, @RequestPart("file") file: MultipartFile): FileResponse {
        val userId = authentication.apiPrincipal().userId
        val url = storage.store(file, "avatars/$userId", setOf("image/jpeg", "image/png", "image/webp"))
        service.updateStoredFile(userId, "photo_path", url)
        return FileResponse(url)
    }

    @PostMapping("/cv", consumes = ["multipart/form-data"])
    fun cv(authentication: Authentication, @RequestPart("file") file: MultipartFile): FileResponse {
        val userId = authentication.apiPrincipal().userId
        val url = storage.store(file, "cvs/$userId", setOf("application/pdf"))
        service.updateStoredFile(userId, "cv_path", url)
        return FileResponse(url)
    }

    @PostMapping("/company/representative", consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun representative(
        authentication: Authentication,
        @RequestParam name: String,
        @RequestParam documentType: String,
        @RequestParam documentNumber: String,
        @RequestPart("file", required = false) file: MultipartFile?,
    ) {
        val userId = authentication.apiPrincipal().userId
        val url = file?.takeIf { !it.isEmpty }?.let {
            storage.store(it, "companies/$userId/representative", setOf("application/pdf"))
        }
        service.saveRepresentative(userId, name, documentType, documentNumber, url)
    }

    @PostMapping("/company/documents", consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun companyDocuments(
        authentication: Authentication,
        @RequestPart("rut", required = false) rut: MultipartFile?,
        @RequestPart("chamber", required = false) chamber: MultipartFile?,
    ) {
        val userId = authentication.apiPrincipal().userId
        val rutUrl = rut?.takeIf { !it.isEmpty }?.let {
            storage.store(it, "companies/$userId/documents", setOf("application/pdf"))
        }
        val chamberUrl = chamber?.takeIf { !it.isEmpty }?.let {
            storage.store(it, "companies/$userId/documents", setOf("application/pdf"))
        }
        service.saveCompanyDocuments(userId, rutUrl, chamberUrl)
    }
}
