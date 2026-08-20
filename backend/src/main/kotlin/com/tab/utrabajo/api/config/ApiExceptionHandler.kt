package com.tab.utrabajo.api.config

import com.tab.utrabajo.api.auth.ConflictException
import com.tab.utrabajo.api.auth.ForbiddenException
import com.tab.utrabajo.api.auth.NotFoundException
import com.tab.utrabajo.api.auth.UnauthorizedException
import com.tab.utrabajo.api.model.ApiError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(error: UnauthorizedException) = response(HttpStatus.UNAUTHORIZED, "unauthorized", error.message)

    @ExceptionHandler(ForbiddenException::class)
    fun forbidden(error: ForbiddenException) = response(HttpStatus.FORBIDDEN, "forbidden", error.message)

    @ExceptionHandler(NotFoundException::class)
    fun notFound(error: NotFoundException) = response(HttpStatus.NOT_FOUND, "not_found", error.message)

    @ExceptionHandler(ConflictException::class)
    fun conflict(error: ConflictException) = response(HttpStatus.CONFLICT, "conflict", error.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(error: IllegalArgumentException) = response(HttpStatus.BAD_REQUEST, "bad_request", error.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(error: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = error.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Datos inválidos"
        return response(HttpStatus.BAD_REQUEST, "validation_error", message)
    }

    private fun response(status: HttpStatus, code: String, message: String?) =
        ResponseEntity.status(status).body(ApiError(code, message ?: status.reasonPhrase))
}
