package com.chat.auth.filter

import com.chat.auth.dto.LoginDto
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter

class EmailPasswordAuthFilter(
    val loginUrl: String,
    private val objectMapper: ObjectMapper,
) : AbstractAuthenticationProcessingFilter(loginUrl) {
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication? {

        val (email, password) = objectMapper.readValue(request.inputStream, LoginDto::class.java)

        val token = UsernamePasswordAuthenticationToken.unauthenticated(email, password)

        token.details = super.authenticationDetailsSource.buildDetails(request)

        return super.authenticationManager.authenticate(token)
    }

}