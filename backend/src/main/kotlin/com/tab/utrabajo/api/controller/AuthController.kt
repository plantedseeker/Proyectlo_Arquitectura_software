package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.auth.AuthService
import com.tab.utrabajo.api.model.AuthResponse
import com.tab.utrabajo.api.model.LoginRequest
import com.tab.utrabajo.api.model.RegisterCompanyRequest
import com.tab.utrabajo.api.model.RegisterStudentRequest
import com.tab.utrabajo.api.model.UserView
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerStudent(@Valid @RequestBody request: RegisterStudentRequest): AuthResponse =
        authService.registerStudent(request)

    @PostMapping("/register/company")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerCompany(@Valid @RequestBody request: RegisterCompanyRequest): AuthResponse =
        authService.registerCompany(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        authService.login(request.email, request.password)

    @GetMapping("/me")
    fun me(authentication: Authentication): UserView =
        authService.currentUser(authentication.apiPrincipal().userId)

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(authentication: Authentication) {
        authService.logout(authentication.credentials.toString())
    }
}
