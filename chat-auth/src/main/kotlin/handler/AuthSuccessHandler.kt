package com.chat.auth.handler

import com.chat.auth.application.CustomUserPrincipal
import com.chat.auth.dto.ApiResponseDto
import com.chat.auth.dto.UserResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class AuthSuccessHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {
//    private val loginUsers: LoginUsers? = null

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
//        val loginUser: LoginUser = addLoginUser(request, authentication)

        response.setContentType(MediaType.APPLICATION_JSON_VALUE)
        response.setStatus(HttpServletResponse.SC_OK)

        val principal = authentication.principal as CustomUserPrincipal
        val userInfo = principal.attribute

        val userData =
            objectMapper.writeValueAsString(
                ApiResponseDto(
                    UserResponseDto(
                        userInfo.userId,
                        userInfo.email,
                        userInfo.nickname
                    ),
                )
            )

        response.getWriter().write(userData)
    }
}
