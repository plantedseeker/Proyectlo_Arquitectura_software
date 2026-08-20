package com.tab.utrabajo.api.controller

import com.tab.utrabajo.api.auth.ApiPrincipal
import com.tab.utrabajo.api.auth.UnauthorizedException
import org.springframework.security.core.Authentication

fun Authentication.apiPrincipal(): ApiPrincipal =
    principal as? ApiPrincipal ?: throw UnauthorizedException("Sesión inválida")
