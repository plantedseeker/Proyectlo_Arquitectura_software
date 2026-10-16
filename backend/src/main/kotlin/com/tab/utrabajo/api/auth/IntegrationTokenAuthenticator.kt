package com.tab.utrabajo.api.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class IntegrationTokenAuthenticator(
    @Value("\${app.integration-token:}") private val configuredToken: String,
) {
    fun requireValid(providedToken: String?) {
        val valid = configuredToken.isNotBlank() &&
            !providedToken.isNullOrBlank() &&
            MessageDigest.isEqual(
                configuredToken.toByteArray(Charsets.UTF_8),
                providedToken.toByteArray(Charsets.UTF_8),
            )

        if (!valid) {
            throw UnauthorizedException("Credencial de integración ausente o inválida")
        }
    }
}
