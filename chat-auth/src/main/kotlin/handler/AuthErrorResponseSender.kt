package com.chat.auth.handler

import com.chat.auth.dto.ApiResponseDto
import com.chat.auth.dto.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


object AuthErrorResponseSender {

    fun sendError(
        response: HttpServletResponse,
        exception: Exception,
        status: HttpStatus,
        objectMapper: ObjectMapper
    ) {

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val errorInfo = ErrorResponse(status.value(), exception.message ?: "인증 거절")

        val errorResponse = objectMapper.writeValueAsString(ApiResponseDto(errorInfo))

        response.writer.write(errorResponse)
    }
}

